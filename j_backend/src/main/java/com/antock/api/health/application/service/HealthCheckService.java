package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.application.dto.PagedSystemHealthResponse;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.domain.SystemHealth;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import com.antock.api.health.infrastructure.SystemHealthRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckService {

    private final HealthCheckRepository healthCheckRepository;
    private final SystemHealthRepository systemHealthRepository;
    private final HealthCheckOrchestrator healthCheckOrchestrator;
    private final ObjectMapper objectMapper;
    private final Executor asyncExecutor;

    @Value("${health.check.timeout:5000}")
    private long healthCheckTimeout;

    @Value("${health.cache.duration:300}")
    private long cacheDurationSeconds;


    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth(HealthCheckRequest request) {
        List<String> components = request.getComponents() != null 
                ? request.getComponents() 
                : healthCheckOrchestrator.getAvailableComponents();

        if (request.isIgnoreCache()) {
            return performSystemHealthCheckInNewTransaction(components, request.getCheckType());
        }

        Optional<SystemHealth> cachedSystemHealth = systemHealthRepository
                .findLatestValidSystemHealth(LocalDateTime.now());
        if (cachedSystemHealth.isPresent() && !cachedSystemHealth.get().isExpired()) {
            return convertToSystemHealthResponseFromComponents(cachedSystemHealth.get(), components);
        }

        return performSystemHealthCheckInNewTransaction(components, request.getCheckType());
    }

    @Transactional
    public SystemHealthResponse performSystemHealthCheck(List<String> components, String checkType) {
        List<CompletableFuture<HealthCheck>> futures = components.stream()
                .map(component -> CompletableFuture.supplyAsync(() -> performComponentHealthCheck(component, checkType),
                        asyncExecutor))
                .collect(Collectors.toList());

        List<HealthCheck> healthChecks = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        SystemHealth systemHealth = calculateSystemHealth(healthChecks, checkType);
        systemHealthRepository.save(systemHealth);

        healthCheckRepository.saveAll(healthChecks);

        return convertToSystemHealthResponse(systemHealth, healthChecks);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SystemHealthResponse performSystemHealthCheckInNewTransaction(List<String> components, String checkType) {
        return performSystemHealthCheck(components, checkType);
    }

    @Transactional
    public HealthCheck performComponentHealthCheck(String component, String checkType) {
        try {
            return healthCheckOrchestrator.performComponentHealthCheck(component, checkType);
        } catch (BusinessException e) {
            log.error("헬스 체크 비즈니스 오류 - component: {}, error: {}", component, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("헬스 체크 예외 발생 - component: {}", component, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "헬스 체크 중 예상치 못한 오류 발생: " + e.getMessage(), e);
        }
    }

    private SystemHealth calculateSystemHealth(List<HealthCheck> healthChecks, String checkType) {
        int totalComponents = healthChecks.size();
        int healthyComponents = (int) healthChecks.stream().filter(h -> HealthStatus.UP.equals(h.getStatus())).count();
        int unhealthyComponents = (int) healthChecks.stream().filter(h -> HealthStatus.DOWN.equals(h.getStatus()))
                .count();
        int unknownComponents = (int) healthChecks.stream().filter(h -> HealthStatus.UNKNOWN.equals(h.getStatus()))
                .count();

        HealthStatus overallStatus;
        if (unhealthyComponents > 0) {
            overallStatus = HealthStatus.DOWN;
        } else if (unknownComponents > 0) {
            overallStatus = HealthStatus.UNKNOWN;
        } else {
            overallStatus = HealthStatus.UP;
        }

        Map<String, Object> details = new HashMap<>();
        Map<String, Map<String, Object>> componentDetails = new HashMap<>();
        for (HealthCheck h : healthChecks) {
            Map<String, Object> componentInfo = new HashMap<>();
            componentInfo.put("status", h.getStatus());
            componentInfo.put("message", h.getMessage());
            componentInfo.put("responseTime", h.getResponseTime());
            componentDetails.put(h.getComponent(), componentInfo);
        }
        details.put("components", componentDetails);

        return SystemHealth.builder()
                .overallStatus(overallStatus)
                .totalComponents(totalComponents)
                .healthyComponents(healthyComponents)
                .unhealthyComponents(unhealthyComponents)
                .unknownComponents(unknownComponents)
                .details(convertDetailsToJson(details))
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(cacheDurationSeconds))
                .build();
    }

    @Transactional(readOnly = true)
    public List<HealthCheckResponse> getComponentHealth(String component) {
        List<HealthCheck> healthChecks = healthCheckRepository.findByComponentOrderByCheckedAtDesc(component);
        return healthChecks.stream()
                .map(this::convertToHealthCheckResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckResponse> getComponentHealth(String component, Pageable pageable) {
        Page<HealthCheck> healthChecks = healthCheckRepository.findByComponentOrderByCheckedAtDesc(component, pageable);
        return healthChecks.map(this::convertToHealthCheckResponse);
    }

    @Transactional(readOnly = true)
    public List<HealthCheckResponse> getHealthHistory(LocalDateTime fromDate) {
        List<HealthCheck> healthChecks = healthCheckRepository.findByCheckedAtAfterOrderByCheckedAtDesc(fromDate);
        return healthChecks.stream()
                .map(this::convertToHealthCheckResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckResponse> getHealthHistory(LocalDateTime fromDate, Pageable pageable) {
        Page<HealthCheck> healthChecks = healthCheckRepository.findRecentChecks(fromDate, pageable);
        return healthChecks.map(this::convertToHealthCheckResponse);
    }

    @Transactional
    public void cleanupExpiredChecks() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deletedCount = healthCheckRepository.deleteOldChecks(cutoffDate);
        int deletedSystemHealthCount = systemHealthRepository.deleteOldSystemHealth(cutoffDate);
    }

    private HealthCheckResponse convertToHealthCheckResponse(HealthCheck healthCheck) {
        return HealthCheckResponse.builder()
                .component(healthCheck.getComponent())
                .status(healthCheck.getStatus())
                .statusDescription(healthCheck.getStatus().getDescription())
                .message(healthCheck.getMessage())
                .responseTime(healthCheck.getResponseTime())
                .checkType(healthCheck.getCheckType())
                .details(parseDetailsFromJson(healthCheck.getDetails()))
                .checkedAt(healthCheck.getCheckedAt())
                .expiresAt(healthCheck.getExpiresAt())
                .expired(healthCheck.isExpired())
                .healthy(healthCheck.isUp())
                .build();
    }

    private SystemHealthResponse convertToSystemHealthResponseFromComponents(SystemHealth systemHealth,
                                                                             List<String> components) {
        List<HealthCheck> recentChecks = healthCheckRepository.findByComponentInOrderByCheckedAtDesc(components);

        return SystemHealthResponse.builder()
                .overallStatus(systemHealth.getOverallStatus())
                .overallStatusDescription(systemHealth.getOverallStatus().getDescription())
                .totalComponents(systemHealth.getTotalComponents())
                .healthyComponents(systemHealth.getHealthyComponents())
                .unhealthyComponents(systemHealth.getUnhealthyComponents())
                .unknownComponents(systemHealth.getUnknownComponents())
                .healthPercentage(systemHealth.getHealthPercentage())
                .details(parseDetailsFromJson(systemHealth.getDetails()))
                .components(recentChecks.stream().map(this::convertToHealthCheckResponse).collect(Collectors.toList()))
                .checkedAt(systemHealth.getCheckedAt())
                .expiresAt(systemHealth.getExpiresAt())
                .expired(systemHealth.isExpired())
                .healthy(systemHealth.isHealthy())
                .build();
    }

    private SystemHealthResponse convertToSystemHealthResponse(SystemHealth systemHealth,
                                                               List<HealthCheck> healthChecks) {
        return SystemHealthResponse.builder()
                .overallStatus(systemHealth.getOverallStatus())
                .overallStatusDescription(systemHealth.getOverallStatus().getDescription())
                .totalComponents(systemHealth.getTotalComponents())
                .healthyComponents(systemHealth.getHealthyComponents())
                .unhealthyComponents(systemHealth.getUnhealthyComponents())
                .unknownComponents(systemHealth.getUnknownComponents())
                .healthPercentage(systemHealth.getHealthPercentage())
                .details(parseDetailsFromJson(systemHealth.getDetails()))
                .components(healthChecks.stream().map(this::convertToHealthCheckResponse).collect(Collectors.toList()))
                .checkedAt(systemHealth.getCheckedAt())
                .expiresAt(systemHealth.getExpiresAt())
                .expired(systemHealth.isExpired())
                .healthy(systemHealth.isHealthy())
                .build();
    }

    private String convertDetailsToJson(Map<String, Object> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDetailsFromJson(String detailsJson) {
        try {
            if (detailsJson == null || detailsJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(detailsJson, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private SystemHealth getLatestSystemHealth() {
        return systemHealthRepository.findTopByOrderByCheckedAtDesc()
                .orElse(null);
    }

    @Cacheable(value = "systemHealthPaged", key = "#page + '_' + #size + '_' + #groupBy")
    public PagedSystemHealthResponse getSystemHealthPaged(int page, int size, String groupBy) {
        log.info("페이징된 시스템 헬스 체크 시작 - page: {}, size: {}, groupBy: {}", page, size, groupBy);

        try {
            SystemHealth systemHealth = getLatestSystemHealth();
            if (systemHealth == null) {
                log.warn("최신 시스템 헬스 정보가 없습니다. 빈 응답을 반환합니다.");
                return createEmptyPagedResponse();
            }

            log.debug("최신 시스템 헬스 정보 조회 완료: {}", systemHealth.getId());
            List<HealthCheck> allHealthChecks = healthCheckRepository.findAllOrderByCheckedAtDesc();
            log.debug("전체 헬스 체크 데이터 조회 완료: {}개", allHealthChecks.size());

            if (allHealthChecks.size() > 10000) {
                log.warn("헬스 체크 데이터가 너무 많습니다 ({}개). 최근 1000개만 처리합니다.", allHealthChecks.size());
                allHealthChecks = allHealthChecks.subList(0, 1000);
            }

            Map<String, List<HealthCheck>> groupedComponents = groupComponents(allHealthChecks, groupBy);
            log.debug("컴포넌트 그룹핑 완료: {}개 그룹", groupedComponents.size());

            List<HealthCheck> pagedComponents = applyPaging(allHealthChecks, page, size);
            log.debug("페이징 처리 완료: {}개 컴포넌트", pagedComponents.size());

            PagedSystemHealthResponse.PaginationInfo pagination = createPaginationInfo(page, size,
                    allHealthChecks.size());

            Map<String, PagedSystemHealthResponse.ComponentGroupInfo> componentGroups = createComponentGroups(
                    groupedComponents);

            log.debug("페이징된 시스템 헬스 응답 생성 완료");
            return PagedSystemHealthResponse.builder()
                    .overallStatus(systemHealth.getOverallStatus())
                    .overallStatusDescription(systemHealth.getOverallStatus().getDescription())
                    .totalComponents(systemHealth.getTotalComponents())
                    .healthyComponents(systemHealth.getHealthyComponents())
                    .unhealthyComponents(systemHealth.getUnhealthyComponents())
                    .unknownComponents(systemHealth.getUnknownComponents())
                    .healthPercentage(systemHealth.getHealthPercentage())
                    .details(parseDetailsFromJson(systemHealth.getDetails()))
                    .components(
                            pagedComponents.stream().map(this::convertToHealthCheckResponse)
                                    .collect(Collectors.toList()))
                    .pagination(pagination)
                    .componentGroups(componentGroups)
                    .checkedAt(systemHealth.getCheckedAt())
                    .expiresAt(systemHealth.getExpiresAt())
                    .expired(systemHealth.isExpired())
                    .healthy(systemHealth.isHealthy())
                    .build();
        } catch (BusinessException e) {
            log.error("페이징된 시스템 헬스 체크 비즈니스 오류 발생", e);
            throw e;
        } catch (Exception e) {
            log.error("페이징된 시스템 헬스 체크 중 예상치 못한 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "페이징된 시스템 헬스 체크 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private Map<String, List<HealthCheck>> groupComponents(List<HealthCheck> healthChecks, String groupBy) {
        if ("component".equals(groupBy)) {
            return healthChecks.stream()
                    .collect(Collectors.groupingBy(HealthCheck::getComponent));
        } else if ("status".equals(groupBy)) {
            return healthChecks.stream()
                    .collect(Collectors.groupingBy(hc -> hc.getStatus().getCode()));
        } else {
            return healthChecks.stream()
                    .collect(Collectors.groupingBy(HealthCheck::getComponent));
        }
    }

    private List<HealthCheck> applyPaging(List<HealthCheck> healthChecks, int page, int size) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, healthChecks.size());

        if (startIndex >= healthChecks.size()) {
            return new ArrayList<>();
        }

        return healthChecks.subList(startIndex, endIndex);
    }

    private PagedSystemHealthResponse.PaginationInfo createPaginationInfo(int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PagedSystemHealthResponse.PaginationInfo.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .nextPage(page < totalPages - 1 ? page + 1 : null)
                .previousPage(page > 0 ? page - 1 : null)
                .numberOfElements(Math.min(size, (int) (totalElements - page * size)))
                .build();
    }

    private Map<String, PagedSystemHealthResponse.ComponentGroupInfo> createComponentGroups(
            Map<String, List<HealthCheck>> groupedComponents) {

        return groupedComponents.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<HealthCheck> groupChecks = entry.getValue();
                            int totalCount = groupChecks.size();
                            int healthyCount = (int) groupChecks.stream()
                                    .filter(hc -> HealthStatus.UP.equals(hc.getStatus()))
                                    .count();
                            int unhealthyCount = totalCount - healthyCount;

                            HealthStatus groupStatus = unhealthyCount > 0 ? HealthStatus.DOWN : HealthStatus.UP;
                            String groupStatusDescription = unhealthyCount > 0
                                    ? "일부 장애 (" + unhealthyCount + "/" + totalCount + ")"
                                    : "정상 (" + healthyCount + "/" + totalCount + ")";

                            return PagedSystemHealthResponse.ComponentGroupInfo.builder()
                                    .groupName(entry.getKey())
                                    .totalCount(totalCount)
                                    .healthyCount(healthyCount)
                                    .unhealthyCount(unhealthyCount)
                                    .groupStatus(groupStatus)
                                    .groupStatusDescription(groupStatusDescription)
                                    .build();
                        }));
    }

    private PagedSystemHealthResponse createEmptyPagedResponse() {
        return PagedSystemHealthResponse.builder()
                .overallStatus(HealthStatus.UNKNOWN)
                .overallStatusDescription("시스템 헬스 정보를 찾을 수 없습니다")
                .totalComponents(0)
                .healthyComponents(0)
                .unhealthyComponents(0)
                .unknownComponents(0)
                .healthPercentage(0.0)
                .details(new HashMap<>())
                .components(new ArrayList<>())
                .pagination(PagedSystemHealthResponse.PaginationInfo.builder()
                        .pageNumber(0)
                        .pageSize(10)
                        .totalElements(0)
                        .totalPages(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .nextPage(null)
                        .previousPage(null)
                        .numberOfElements(0)
                        .build())
                .componentGroups(new HashMap<>())
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .expired(false)
                .healthy(false)
                .build();
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableComponents() {
        try {
            List<String> components = healthCheckRepository.findDistinctComponents();
            log.debug("사용 가능한 컴포넌트 조회 완료: {}개", components.size());
            List<String> availableComponents = healthCheckOrchestrator.getAvailableComponents();
            return components.isEmpty() ? availableComponents : components;
        } catch (Exception e) {
            log.warn("컴포넌트 목록 조회 실패, 기본 컴포넌트 사용: {}", e.getMessage());
            return healthCheckOrchestrator.getAvailableComponents();
        }
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckResponse> getHealthHistoryWithFilters(
            LocalDateTime fromDate, LocalDateTime toDate, String component, String status, Pageable pageable) {
        try {
            log.info("필터링된 헬스 체크 이력 조회 - fromDate: {}, toDate: {}, component: {}, status: {}",
                    fromDate, toDate, component, status);

            Page<HealthCheck> healthChecks;

            if (component != null && !component.isEmpty() && status != null && !status.isEmpty()) {
                HealthStatus healthStatus = HealthStatus.valueOf(status);
                healthChecks = healthCheckRepository.findByComponentAndStatusAndDateRange(
                        component, healthStatus, fromDate, toDate, pageable);
            } else if (component != null && !component.isEmpty()) {
                healthChecks = healthCheckRepository.findByComponentAndDateRange(
                        component, fromDate, toDate, pageable);
            } else if (status != null && !status.isEmpty()) {
                HealthStatus healthStatus = HealthStatus.valueOf(status);
                healthChecks = healthCheckRepository.findByStatusAndDateRange(
                        healthStatus, fromDate, toDate, pageable);
            } else {
                healthChecks = healthCheckRepository.findByDateRange(fromDate, toDate, pageable);
            }

            log.debug("필터링된 헬스 체크 이력 조회 완료: {}개", healthChecks.getTotalElements());

            return healthChecks.map(this::convertToHealthCheckResponse);

        } catch (BusinessException e) {
            log.error("필터링된 헬스 체크 이력 조회 비즈니스 오류", e);
            throw e;
        } catch (Exception e) {
            log.error("필터링된 헬스 체크 이력 조회 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "필터링된 헬스 체크 이력 조회 실패: " + e.getMessage(), e);
        }
    }
}