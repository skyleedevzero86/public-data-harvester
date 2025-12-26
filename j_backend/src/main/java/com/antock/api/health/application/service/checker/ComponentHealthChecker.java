package com.antock.api.health.application.service.checker;

import com.antock.api.health.domain.HealthCheckResult;

public interface ComponentHealthChecker {
    String getComponentName();
    HealthCheckResult check();
}

