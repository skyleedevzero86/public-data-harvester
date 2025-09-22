package com.antock.api.health.application.dto;


import com.antock.api.health.domain.HealthStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
private static class HealthCheckResult {
    private HealthStatus status;
    private String message;
    private Map<String, Object> details;
}