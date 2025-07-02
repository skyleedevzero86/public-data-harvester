package com.antock.api.member.application.service;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RedisRateLimitService implements RateLimitServiceInterface {


    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Value("${custom.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${custom.security.rate-limit.burst-capacity:100}")
    private int burstCapacity;

    @Value("${custom.redis.rate-limit-key-prefix:rate_limit:}")
    private String keyPrefix;

    @Value("${custom.redis.enabled:false}")
    private boolean redisEnabled;

    // 메모리 기반 fallback
    private final ConcurrentHashMap<String, RateLimitInfo> memoryCache = new ConcurrentHashMap<>();
    private boolean useRedis = false;

    @PostConstruct
    public void init() {
        // Redis 사용 가능 여부 확인
        useRedis = redisEnabled && redisTemplate != null && checkRedisConnection();

        if (useRedis) {
            log.info("Redis 백엔드로 RateLimitService가 초기화되었습니다.");
        } else {
            log.info("메모리 백엔드로 RateLimitService가 초기화되었습니다 (Redis 사용 불가).");
        }
    }

    @Override
    public boolean isRedisAvailable() {
        return useRedis;  // 또는 checkRedisConnection() 호출
    }

    @Override
    public void checkRateLimit(String identifier, String action) {
        if (useRedis) {
            checkRateLimitWithRedis(identifier, action);
        } else {
            checkRateLimitWithMemory(identifier, action);
        }
    }

    @Override
    public int getCurrentCount(String identifier, String action) {
        if (useRedis) {
            return getCurrentCountWithRedis(identifier, action);
        } else {
            return getCurrentCountWithMemory(identifier, action);
        }
    }

    @Override
    public void resetLimit(String identifier, String action) {
        if (useRedis) {
            resetLimitWithRedis(identifier, action);
        } else {
            resetLimitWithMemory(identifier, action);
        }
    }

    // Redis 기반 메서드들
    private void checkRateLimitWithRedis(String identifier, String action) {
        String key = generateKey(identifier, action);

        try {
            String countStr = redisTemplate.opsForValue().get(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

            if (currentCount >= burstCapacity) {
                log.warn("식별자: {}, 작업: {}에 대한 Redis 속도 제한을 초과했습니다: 현재 횟수 {}", identifier, action, currentCount);
                throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
            }

            // 카운트 증가
            Long newCount = redisTemplate.opsForValue().increment(key);

            // 첫 번째 요청인 경우 TTL 설정
            if (newCount != null && newCount == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            log.debug("{}에 대한 Redis 속도 제한 검사를 통과했습니다: {}/{}", identifier, newCount, burstCapacity);

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            log.error("Redis 오류, 메모리로 대체합니다: {}", e.getMessage());
            // Redis 실패 시 메모리로 fallback
            checkRateLimitWithMemory(identifier, action);
        }
    }

    private int getCurrentCountWithRedis(String identifier, String action) {
        try {
            String key = generateKey(identifier, action);
            String countStr = redisTemplate.opsForValue().get(key);
            return countStr != null ? Integer.parseInt(countStr) : 0;
        } catch (Exception e) {
            log.error("Redis에서 현재 카운트를 가져오지 못했습니다: {}", e.getMessage());
            return getCurrentCountWithMemory(identifier, action);
        }
    }

    private void resetLimitWithRedis(String identifier, String action) {
        try {
            String key = generateKey(identifier, action);
            redisTemplate.delete(key);
            log.info("식별자: {}, 작업: {}에 대한 Redis 속도 제한이 재설정되었습니다.", identifier, action);
        } catch (Exception e) {
            log.error("Redis에서 속도 제한 재설정에 실패했습니다: {}", e.getMessage());
            resetLimitWithMemory(identifier, action);
        }
    }

    // 메모리 기반 메서드들
    private void checkRateLimitWithMemory(String identifier, String action) {
        String key = generateKey(identifier, action);
        LocalDateTime now = LocalDateTime.now();

        memoryCache.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired(now)) {
                return new RateLimitInfo(1, now);
            } else {
                existing.incrementCount();
                return existing;
            }
        });

        RateLimitInfo info = memoryCache.get(key);
        if (info.getCount() > burstCapacity) {
            log.warn("식별자: {}, 작업: {}, 횟수: {}에 대한 메모리 속도 제한을 초과했습니다.", identifier, action, info.getCount());
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        log.debug("{}에 대한 메모리 속도 제한 검사를 통과했습니다: {}/{}", identifier, info.getCount(), burstCapacity);

        // 간단한 정리
        if (memoryCache.size() > 10000) {
            cleanupExpiredEntries(now);
        }
    }

    private int getCurrentCountWithMemory(String identifier, String action) {
        String key = generateKey(identifier, action);
        RateLimitInfo info = memoryCache.get(key);
        if (info == null || info.isExpired(LocalDateTime.now())) {
            return 0;
        }
        return info.getCount();
    }

    private void resetLimitWithMemory(String identifier, String action) {
        String key = generateKey(identifier, action);
        memoryCache.remove(key);
        log.info("식별자: {}, 작업: {}에 대한 메모리 속도 제한이 재설정되었습니다.", identifier, action);
    }

    // 유틸리티 메서드들
    private boolean checkRedisConnection() {
        if (redisTemplate == null) return false;

        try {
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(5));
            String result = redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");
            return "ok".equals(result);
        } catch (Exception e) {
            log.debug("Redis 연결 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    private String generateKey(String identifier, String action) {
        return keyPrefix + action + ":" + identifier;
    }

    private void cleanupExpiredEntries(LocalDateTime now) {
        memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    // 내부 클래스 (메모리 캐시용)
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