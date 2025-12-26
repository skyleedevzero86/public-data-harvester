package com.antock.api.health.application.service;

import com.antock.api.health.application.service.checker.ComponentHealthChecker;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HealthCheckOrchestrator {

    private final List<ComponentHealthChecker> healthCheckers;
    private final ObjectMapper objectMapper;

    @Value("${health.cache.duration:300}")
    private long cacheDurationSeconds;

    public HealthCheck performComponentHealthCheck(String component, String checkType) {
        long startTime = System.currentTimeMillis();
        HealthStatus status = HealthStatus.UNKNOWN;
        String message = "";
        Map<String, Object> details = new HashMap<>();

        try {
            ComponentHealthChecker checker = findChecker(component);
            HealthCheckResult result = checker.check();
            status = result.getStatus();
            message = result.getMessage();
            details = result.getDetails();
        } catch (BusinessException e) {
            status = HealthStatus.DOWN;
            message = "헬스 체크 중 비즈니스 오류 발생: " + e.getMessage();
            details.put("error", e.getErrorCode().getCode());
            details.put("errorMessage", e.getMessage());
            log.warn("헬스 체크 비즈니스 오류 - component: {}, error: {}", component, e.getMessage());
        } catch (Exception e) {
            status = HealthStatus.DOWN;
            message = "헬스 체크 중 예상치 못한 오류 발생: " + e.getMessage();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            log.error("헬스 체크 예외 발생 - component: {}", component, e);
        }

        long responseTime = System.currentTimeMillis() - startTime;

        return HealthCheck.builder()
                .component(component)
                .status(status)
                .message(message)
                .responseTime(responseTime)
                .checkType(checkType != null ? checkType : "manual")
                .details(convertDetailsToJson(details))
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(cacheDurationSeconds))
                .build();
    }

    private ComponentHealthChecker findChecker(String component) {
        return healthCheckers.stream()
                .filter(checker -> checker.getComponentName().equalsIgnoreCase(component))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                        "알 수 없는 컴포넌트: " + component));
    }

    public List<String> getAvailableComponents() {
        return healthCheckers.stream()
                .map(ComponentHealthChecker::getComponentName)
                .collect(Collectors.toList());
    }

    private String convertDetailsToJson(Map<String, Object> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("헬스 체크 상세 정보 JSON 변환 실패", e);
            return "{}";
        }
    }
}

