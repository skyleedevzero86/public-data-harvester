package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.service.converter.HealthCheckResponseConverter;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import com.antock.api.health.application.service.HealthCheckOrchestrator;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckQueryService {

    private final HealthCheckRepository healthCheckRepository;
    private final HealthCheckOrchestrator healthCheckOrchestrator;
    private final HealthCheckResponseConverter responseConverter;

    @Transactional(readOnly = true)
    public List<HealthCheckResponse> getComponentHealth(String component) {
        List<HealthCheck> healthChecks = healthCheckRepository.findByComponentOrderByCheckedAtDesc(component);
        return healthChecks.stream()
                .map(responseConverter::convertToHealthCheckResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckResponse> getComponentHealth(String component, Pageable pageable) {
        Page<HealthCheck> healthChecks = healthCheckRepository.findByComponentOrderByCheckedAtDesc(component, pageable);
        return healthChecks.map(responseConverter::convertToHealthCheckResponse);
    }

    @Transactional(readOnly = true)
    public List<HealthCheckResponse> getHealthHistory(LocalDateTime fromDate) {
        List<HealthCheck> healthChecks = healthCheckRepository.findByCheckedAtAfterOrderByCheckedAtDesc(fromDate);
        return healthChecks.stream()
                .map(responseConverter::convertToHealthCheckResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckResponse> getHealthHistory(LocalDateTime fromDate, Pageable pageable) {
        Page<HealthCheck> healthChecks = healthCheckRepository.findRecentChecks(fromDate, pageable);
        return healthChecks.map(responseConverter::convertToHealthCheckResponse);
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

            return healthChecks.map(responseConverter::convertToHealthCheckResponse);

        } catch (BusinessException e) {
            log.error("필터링된 헬스 체크 이력 조회 비즈니스 오류", e);
            throw e;
        } catch (Exception e) {
            log.error("필터링된 헬스 체크 이력 조회 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "필터링된 헬스 체크 이력 조회 실패: " + e.getMessage(), e);
        }
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
}
