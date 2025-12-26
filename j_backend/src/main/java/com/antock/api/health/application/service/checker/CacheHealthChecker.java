package com.antock.api.health.application.service.checker;

import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.member.application.service.MemberApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheHealthChecker implements ComponentHealthChecker {

    private final MemberApplicationService memberApplicationService;

    @Override
    public String getComponentName() {
        return "cache";
    }

    @Override
    public HealthCheckResult check() {
        try {
            var cacheStats = memberApplicationService.getCacheStatistics();
            Map<String, Object> details = new HashMap<>();
            details.put("cacheStats", cacheStats);

            if (cacheStats != null) {
                return new HealthCheckResult(HealthStatus.UP, "캐시 서비스 정상", details, 0L);
            } else {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("error", "CACHE_STATS_NULL");
                errorDetails.put("errorMessage", "캐시 서비스 응답이 null입니다");
                return new HealthCheckResult(HealthStatus.DOWN, "캐시 서비스 응답 없음", errorDetails, 0L);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "캐시 서비스 오류: " + e.getMessage(), details, 0L);
        }
    }
}

