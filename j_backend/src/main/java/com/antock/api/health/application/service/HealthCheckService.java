package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.SystemHealthResponse;
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
import org.springframework.beans.factory.annotation.Value;
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
    private final HealthCheckResponseConverter responseConverter;
    private final Executor asyncExecutor;

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
            List<HealthCheck> recentChecks = healthCheckRepository.findByComponentInOrderByCheckedAtDesc(components);
            return responseConverter.convertToSystemHealthResponseFromComponents(cachedSystemHealth.get(), recentChecks);
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

        return responseConverter.convertToSystemHealthResponse(systemHealth, healthChecks);
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
                .details(responseConverter.convertDetailsToJson(details))
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(cacheDurationSeconds))
                .build();
    }

    @Transactional
    public void cleanupExpiredChecks() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        healthCheckRepository.deleteOldChecks(cutoffDate);
        systemHealthRepository.deleteOldSystemHealth(cutoffDate);
    }
}