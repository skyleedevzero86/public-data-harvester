package com.antock.api.admin.health;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.MemberCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCacheHealthIndicator implements HealthIndicator {

    private final MemberApplicationService memberApplicationService;

    @Override
    public Health health() {
        try {
            MemberCacheService.CacheStatistics stats = memberApplicationService.getCacheStatistics();

            Health.Builder builder = stats.isCacheAvailable() ? Health.up() : Health.down();

            return builder
                    .withDetail("cacheAvailable", stats.isCacheAvailable())
                    .withDetail("totalRequests", stats.getTotalRequests())
                    .withDetail("cacheHits", stats.getCacheHits())
                    .withDetail("cacheMisses", stats.getCacheMisses())
                    .withDetail("hitRate", String.format("%.2f%%", stats.getHitRate()))
                    .withDetail("errors", stats.getCacheErrors())
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "캐시 상태 확인 실패")
                    .build();
        }
    }
}