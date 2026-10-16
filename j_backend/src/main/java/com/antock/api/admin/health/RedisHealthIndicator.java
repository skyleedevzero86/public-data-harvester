package com.antock.api.admin.health;

import com.antock.api.member.application.service.RateLimitServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    public static final Status DEGRADED = new Status("DEGRADED");

    private final RateLimitServiceInterface rateLimitService;
    private final ApplicationEventPublisher eventPublisher;
    private final AtomicBoolean redisUp = new AtomicBoolean(false);
    private final AtomicBoolean sawOutage = new AtomicBoolean(false);

    @Override
    public Health health() {
        return refresh();
    }

    @Scheduled(fixedDelay = 10000)
    public void probeRedis() {
        refresh();
    }

    private Health refresh() {
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
            return degraded("연결 안됨", null);
        } catch (Exception e) {
            markOutage();
            return degraded("오류", e.getMessage());
        }
    }

    private Health degraded(String redisDetail, String error) {
        Health.Builder builder = Health.status(DEGRADED)
                .withDetail("redis", redisDetail)
                .withDetail("backend", "Memory fallback")
                .withDetail("status", "DEGRADED")
                .withDetail("warning", "서버별 메모리 캐시로 요청을 처리 중");
        if (error != null) {
            builder.withDetail("error", error);
        }
        return builder.build();
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
