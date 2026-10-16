package com.antock.api.admin.health;

import com.antock.api.member.application.service.RateLimitServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RateLimitServiceInterface rateLimitService;
    private final ApplicationEventPublisher eventPublisher;
    private final AtomicBoolean redisUp = new AtomicBoolean(false);
    private final AtomicBoolean sawOutage = new AtomicBoolean(false);

    @Override
    public Health health() {
        try {
            boolean connected = rateLimitService.probeRedis();
            if (connected) {
                publishIfRecovered();
                return Health.up()
                        .withDetail("redis", "연결됨")
                        .withDetail("backend", "Redis")
                        .withDetail("status", "UP")
                        .build();
            }
            markOutage();
            return Health.down()
                    .withDetail("redis", "연결 안됨")
                    .withDetail("backend", "Memory fallback")
                    .withDetail("status", "DOWN")
                    .withDetail("warning", "서버별 메모리 캐시로 요청을 처리 중")
                    .build();
        } catch (Exception e) {
            markOutage();
            return Health.down()
                    .withDetail("redis", "오류")
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "DOWN")
                    .build();
        }
    }

    private void publishIfRecovered() {
        boolean wasDown = !redisUp.getAndSet(true);
        if (wasDown && sawOutage.compareAndSet(true, false)) {
            eventPublisher.publishEvent(new RedisRecoveredEvent());
        }
    }

    private void markOutage() {
        redisUp.set(false);
        sawOutage.set(true);
    }
}
