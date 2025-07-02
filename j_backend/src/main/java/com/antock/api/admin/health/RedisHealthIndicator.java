package com.antock.api.admin.health;

import com.antock.api.member.application.service.RateLimitServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RateLimitServiceInterface rateLimitService;

    @Override
    public Health health() {
        try {
            boolean redisAvailable = rateLimitService.isRedisAvailable();

            if (redisAvailable) {
                return Health.up()
                        .withDetail("redis", "연결됨")
                        .withDetail("backend", "Redis")
                        .withDetail("status", "정상")
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", "연결 안됨")
                        .withDetail("backend", "Memory fallback")
                        .withDetail("status", "제한적 동작")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("redis", "오류")
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "비정상")
                    .build();
        }
    }
}