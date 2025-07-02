package com.antock.api.member.application.service;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnMissingBean(RedisRateLimitService.class)
public class InMemoryRateLimitService implements RateLimitServiceInterface {

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${custom.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${custom.security.rate-limit.burst-capacity:100}")
    private int burstCapacity;

    public InMemoryRateLimitService() {
        // 1분마다 만료된 엔트리 정리
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
        log.info("InMemory-기반 RateLimitService가 초기화되었습니다 (Redis 사용 불가).");
    }

    @Override
    public boolean isRedisAvailable() {
        return false; // 메모리 기반이므로 Redis 사용 안함
    }

    @Override
    public void checkRateLimit(String identifier, String action) {
        String key = generateKey(identifier, action);
        LocalDateTime now = LocalDateTime.now();

        rateLimitMap.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired(now)) {
                return new RateLimitInfo(1, now);
            } else {
                existing.incrementCount();
                return existing;
            }
        });

        RateLimitInfo info = rateLimitMap.get(key);
        if (info.getCount() > burstCapacity) {
            log.warn("식별자: {}, 작업: {}, 횟수: {}에 대한 속도 제한을 초과했습니다.",
                    identifier, action, info.getCount());
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        log.debug("{}에 대한 메모리 속도 제한 검사를 통과했습니다: {}/{}", identifier, info.getCount(), burstCapacity);
    }

    @Override
    public int getCurrentCount(String identifier, String action) {
        String key = generateKey(identifier, action);
        RateLimitInfo info = rateLimitMap.get(key);
        if (info == null || info.isExpired(LocalDateTime.now())) {
            return 0;
        }
        return info.getCount();
    }

    @Override
    public void resetLimit(String identifier, String action) {
        String key = generateKey(identifier, action);
        rateLimitMap.remove(key);
        log.info("식별자: {}, 작업: {}에 대한 속도 제한이 재설정되었습니다.", identifier, action);
    }

    private String generateKey(String identifier, String action) {
        return action + ":" + identifier;
    }

    private void cleanupExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        int beforeSize = rateLimitMap.size();
        rateLimitMap.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        int afterSize = rateLimitMap.size();
        if (beforeSize != afterSize) {
            log.debug("만료된 속도 제한 항목 {}개를 정리했습니다.", beforeSize - afterSize);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("InMemoryRateLimitService 종료 완료");
    }

    // 내부 클래스
    private static class RateLimitInfo {
        private int count;
        private final LocalDateTime createdAt;

        public RateLimitInfo(int count, LocalDateTime createdAt) {
            this.count = count;
            this.createdAt = createdAt;
        }

        public void incrementCount() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public boolean isExpired(LocalDateTime now) {
            return createdAt.plusMinutes(1).isBefore(now);
        }
    }
}