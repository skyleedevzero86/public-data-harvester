package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.PagedSystemHealthResponse;
import com.antock.api.health.application.service.converter.HealthCheckResponseConverter;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.domain.SystemHealth;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import com.antock.api.health.infrastructure.SystemHealthRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckPaginationService {

    private final SystemHealthRepository systemHealthRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final HealthCheckResponseConverter responseConverter;

    @Cacheable(value = "systemHealthPaged", key = "#page + '_' + #size + '_' + #groupBy")
    @Transactional(readOnly = true)
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
                    .details(responseConverter.parseDetailsFromJson(systemHealth.getDetails()))
                    .components(
                            pagedComponents.stream().map(responseConverter::convertToHealthCheckResponse)
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

    private SystemHealth getLatestSystemHealth() {
        return systemHealthRepository.findTopByOrderByCheckedAtDesc()
                .orElse(null);
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
}

