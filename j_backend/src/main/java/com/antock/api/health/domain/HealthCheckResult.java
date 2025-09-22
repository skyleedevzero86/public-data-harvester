package com.antock.api.health.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResult {
    private HealthStatus status;
    private String message;
    private Map<String, Object> details;
    private Long responseTime;
}
