package com.antock.api.health.application.service.checker;

import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitHealthChecker implements ComponentHealthChecker {

    private final RateLimitServiceInterface rateLimitService;

    @Override
    public String getComponentName() {
        return "rate-limit";
    }

    @Override
    public HealthCheckResult check() {
        try {
            boolean rateLimitStatus = rateLimitService.isRedisAvailable();
            Map<String, Object> details = new HashMap<>();
            details.put("rateLimitStatus", rateLimitStatus);

            if (rateLimitStatus) {
                return new HealthCheckResult(HealthStatus.UP, "Rate Limit 서비스 정상", details, 0L);
            } else {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("error", "RATE_LIMIT_UNAVAILABLE");
                errorDetails.put("errorMessage", "Rate Limit 서비스가 사용 불가능합니다");
                return new HealthCheckResult(HealthStatus.DOWN, "Rate Limit 서비스 응답 없음", errorDetails, 0L);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "Rate Limit 서비스 오류: " + e.getMessage(), details, 0L);
        }
    }
}

