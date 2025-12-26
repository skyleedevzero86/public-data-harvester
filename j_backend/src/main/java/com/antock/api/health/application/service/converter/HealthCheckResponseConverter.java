package com.antock.api.health.application.service.converter;

import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.SystemHealth;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class HealthCheckResponseConverter {

    private final ObjectMapper objectMapper;

    public HealthCheckResponse convertToHealthCheckResponse(HealthCheck healthCheck) {
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

    public SystemHealthResponse convertToSystemHealthResponse(
            SystemHealth systemHealth,
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
                .components(healthChecks.stream()
                        .map(this::convertToHealthCheckResponse)
                        .collect(Collectors.toList()))
                .checkedAt(systemHealth.getCheckedAt())
                .expiresAt(systemHealth.getExpiresAt())
                .expired(systemHealth.isExpired())
                .healthy(systemHealth.isHealthy())
                .build();
    }

    public SystemHealthResponse convertToSystemHealthResponseFromComponents(
            SystemHealth systemHealth,
            List<HealthCheck> recentChecks) {
        return SystemHealthResponse.builder()
                .overallStatus(systemHealth.getOverallStatus())
                .overallStatusDescription(systemHealth.getOverallStatus().getDescription())
                .totalComponents(systemHealth.getTotalComponents())
                .healthyComponents(systemHealth.getHealthyComponents())
                .unhealthyComponents(systemHealth.getUnhealthyComponents())
                .unknownComponents(systemHealth.getUnknownComponents())
                .healthPercentage(systemHealth.getHealthPercentage())
                .details(parseDetailsFromJson(systemHealth.getDetails()))
                .components(recentChecks.stream()
                        .map(this::convertToHealthCheckResponse)
                        .collect(Collectors.toList()))
                .checkedAt(systemHealth.getCheckedAt())
                .expiresAt(systemHealth.getExpiresAt())
                .expired(systemHealth.isExpired())
                .healthy(systemHealth.isHealthy())
                .build();
    }

    public String convertDetailsToJson(Map<String, Object> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Details를 JSON으로 변환 실패: {}", e.getMessage());
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseDetailsFromJson(String detailsJson) {
        try {
            if (detailsJson == null || detailsJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(detailsJson, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("JSON을 Details로 파싱 실패: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}

