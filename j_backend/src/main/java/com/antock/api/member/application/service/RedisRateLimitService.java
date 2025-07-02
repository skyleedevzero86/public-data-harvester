package com.antock.api.member.application.service;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RedisRateLimitService implements RateLimitServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitService.class);

    @Autowired(required = false)
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${custom.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${custom.security.rate-limit.burst-capacity:100}")
    private int burstCapacity;

    @Value("${custom.redis.rate-limit-key-prefix:rate_limit:}")
    private String keyPrefix;

    @Value("${custom.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${custom.security.rate-limit.memory-cache-limit:1000}")
    private int memoryCacheLimit;

    private final AtomicBoolean useRedis = new AtomicBoolean(false);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicInteger cleanupCount = new AtomicInteger(0);

    private final ConcurrentHashMap<String, RateLimitInfo> memoryCache = new ConcurrentHashMap<>();

    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private static final long HEALTH_CHECK_TIMEOUT_MS = 2000;

    @PostConstruct
    public void init() {
        initializeRedisConnection();
    }

    private void initializeRedisConnection() {
        boolean redisAvailable = redisEnabled && redisTemplate != null && checkRedisConnection();
        useRedis.set(redisAvailable);

        if (redisAvailable) {
            consecutiveFailures.set(0);
            log.info("Redis 백엔드로 RateLimitService가 초기화되었습니다. (host: {}, burst: {})",
                    getRedisHost(), burstCapacity);
        } else {
            log.warn("메모리 백엔드로 RateLimitService가 초기화되었습니다. (Redis 사용 불가, cache limit: {})",
                    memoryCacheLimit);
        }
    }

    @Override
    public boolean isRedisAvailable() {
        return useRedis.get();
    }

    @Override
    public void checkRateLimit(String identifier, String action) {
        if (useRedis.get()) {
            checkRateLimitWithRedis(identifier, action);
        } else {
            checkRateLimitWithMemory(identifier, action);
        }
    }

    @Override
    public int getCurrentCount(String identifier, String action) {
        if (useRedis.get()) {
            return getCurrentCountWithRedis(identifier, action);
        } else {
            return getCurrentCountWithMemory(identifier, action);
        }
    }

    @Override
    public void resetLimit(String identifier, String action) {
        if (useRedis.get()) {
            resetLimitWithRedis(identifier, action);
        } else {
            resetLimitWithMemory(identifier, action);
        }
    }

    private void checkRateLimitWithRedis(String identifier, String action) {
        String key = generateKey(identifier, action);

        try {
            String countStr = redisTemplate.opsForValue().get(key);
            int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

            if (currentCount >= burstCapacity) {
                log.warn("Redis 속도 제한 초과 - 식별자: {}, 작업: {}, 현재: {}, 제한: {}",
                        identifier, action, currentCount, burstCapacity);
                throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
            }

            Long newCount = redisTemplate.opsForValue().increment(key);

            if (newCount != null && newCount == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }

            consecutiveFailures.set(0);

            log.debug("Redis 속도 제한 통과 - 식별자: {}, 카운트: {}/{}",
                    identifier, newCount, burstCapacity);

        } catch (RedisConnectionFailureException e) {
            handleRedisConnectionFailure("Redis 연결 실패", e, identifier, action);
        } catch (org.springframework.dao.QueryTimeoutException e) {
            handleRedisTimeout("Redis 타임아웃", e, identifier, action);
        } catch (RedisSystemException e) {
            handleRedisSystemError("Redis 시스템 오류", e, identifier, action);
        } catch (NumberFormatException e) {
            log.error("Redis 데이터 형식 오류 - key: {}, error: {}", key, e.getMessage());
            redisTemplate.delete(key);
            checkRateLimitWithMemory(identifier, action);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            handleGenericRedisError("Redis 일반 오류", e, identifier, action);
        }
    }

    private void handleRedisConnectionFailure(String message, Exception e, String identifier, String action) {
        int failures = consecutiveFailures.incrementAndGet();
        log.error("{} (연속 실패: {}) - {}", message, failures, e.getMessage());

        if (failures >= MAX_CONSECUTIVE_FAILURES) {
            switchToMemoryFallback("연속 연결 실패 임계값 초과");
        }

        checkRateLimitWithMemory(identifier, action);
    }


    private void handleRedisTimeout(String message, Exception e, String identifier, String action) {
        int failures = consecutiveFailures.incrementAndGet();
        log.warn("{} (연속 실패: {}) - {}", message, failures, e.getMessage());

        if (failures >= MAX_CONSECUTIVE_FAILURES) {
            switchToMemoryFallback("연속 타임아웃 임계값 초과");
        }

        checkRateLimitWithMemory(identifier, action);
    }

    private void handleRedisSystemError(String message, Exception e, String identifier, String action) {
        log.error(" {} - {}", message, e.getMessage());
        consecutiveFailures.incrementAndGet();
        checkRateLimitWithMemory(identifier, action);
    }

    private void handleGenericRedisError(String message, Exception e, String identifier, String action) {
        log.error(" {} - {}", message, e.getMessage());
        consecutiveFailures.incrementAndGet();
        checkRateLimitWithMemory(identifier, action);
    }

    private void switchToMemoryFallback(String reason) {
        if (useRedis.compareAndSet(true, false)) {
            log.warn("Redis에서 메모리 기반으로 전환 - 이유: {}", reason);
        }
    }

    private int getCurrentCountWithRedis(String identifier, String action) {
        try {
            String key = generateKey(identifier, action);
            String countStr = redisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;

            consecutiveFailures.set(0);
            return count;

        } catch (Exception e) {
            log.debug("Redis 현재 카운트 조회 실패 - {}", e.getMessage());
            consecutiveFailures.incrementAndGet();
            return getCurrentCountWithMemory(identifier, action);
        }
    }

    private void resetLimitWithRedis(String identifier, String action) {
        try {
            String key = generateKey(identifier, action);
            Boolean deleted = redisTemplate.delete(key);

            consecutiveFailures.set(0);

            log.info("Redis 속도 제한 재설정 - 식별자: {}, 작업: {}, 삭제됨: {}",
                    identifier, action, deleted);

        } catch (Exception e) {
            log.error("Redis 속도 제한 재설정 실패 - {}", e.getMessage());
            consecutiveFailures.incrementAndGet();
            resetLimitWithMemory(identifier, action);
        }
    }

    private void checkRateLimitWithMemory(String identifier, String action) {
        String key = generateKey(identifier, action);
        LocalDateTime now = LocalDateTime.now();

        if (memoryCache.size() >= memoryCacheLimit) {
            log.warn("메모리 캐시 크기 제한 도달 ({}), 즉시 정리 실행", memoryCacheLimit);
            cleanupExpiredEntries(now);
        }

        memoryCache.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired(now)) {
                return new RateLimitInfo(1, now);
            } else {
                existing.incrementCount();
                return existing;
            }
        });

        RateLimitInfo info = memoryCache.get(key);
        if (info != null && info.getCount() > burstCapacity) {
            log.warn("메모리 속도 제한 초과 - 식별자: {}, 작업: {}, 현재: {}, 제한: {}",
                    identifier, action, info.getCount(), burstCapacity);
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        log.debug("메모리 속도 제한 통과 - 식별자: {}, 카운트: {}/{}",
                identifier, info != null ? info.getCount() : 0, burstCapacity);
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
        RateLimitInfo removed = memoryCache.remove(key);

        log.info("메모리 속도 제한 재설정 - 식별자: {}, 작업: {}, 제거됨: {}",
                identifier, action, removed != null);
    }

    @Scheduled(fixedDelay = 30000)
    public void checkRedisConnectionHealth() {
        if (!redisEnabled || redisTemplate == null) {
            return;
        }

        boolean currentlyUsingRedis = useRedis.get();
        boolean redisHealthy = checkRedisConnection();

        if (!currentlyUsingRedis && redisHealthy) {
            useRedis.set(true);
            consecutiveFailures.set(0);
            log.info("Redis 연결 복구 감지 - 메모리에서 Redis로 전환");
        } else if (currentlyUsingRedis && !redisHealthy) {
            int failures = consecutiveFailures.incrementAndGet();
            if (failures >= MAX_CONSECUTIVE_FAILURES) {
                switchToMemoryFallback("헬스체크 실패");
            }
        }

        if (System.currentTimeMillis() % 300000 < 30000) {
            logConnectionStatus(currentlyUsingRedis, redisHealthy);
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanupMemoryCachePeriodically() {
        if (memoryCache.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int sizeBefore = memoryCache.size();

        cleanupExpiredEntries(now);

        int sizeAfter = memoryCache.size();
        int cleaned = sizeBefore - sizeAfter;

        if (cleaned > 0) {
            cleanupCount.addAndGet(cleaned);
            log.info("메모리 캐시 정리 완료 - 제거: {}, 남은 항목: {}, 총 정리: {}",
                    cleaned, sizeAfter, cleanupCount.get());
        }
    }

    private boolean checkRedisConnection() {
        if (redisTemplate == null) {
            return false;
        }

        try {
            long startTime = System.currentTimeMillis();

            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(5));
            String result = redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");

            long elapsed = System.currentTimeMillis() - startTime;

            if ("ok".equals(result)) {
                if (elapsed > HEALTH_CHECK_TIMEOUT_MS) {
                    log.warn("Redis 응답 시간 지연 - {}ms", elapsed);
                }
                return true;
            }

            return false;

        } catch (Exception e) {
            log.debug("Redis 헬스체크 실패 - {}", e.getMessage());
            return false;
        }
    }

    private void cleanupExpiredEntries(LocalDateTime now) {
        memoryCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired(now);
            if (expired) {
                log.trace("🗑️ 만료된 캐시 항목 제거 - key: {}", entry.getKey());
            }
            return expired;
        });
    }

    private void logConnectionStatus(boolean currentlyUsingRedis, boolean redisHealthy) {
        String backend = currentlyUsingRedis ? "Redis" : "Memory";
        String health = redisHealthy ? "정상" : "비정상";
        int failures = consecutiveFailures.get();
        int cacheSize = memoryCache.size();

        log.info("Rate Limit 상태 - 백엔드: {}, Redis 상태: {}, 연속실패: {}, 메모리캐시: {}",
                backend, health, failures, cacheSize);
    }

    private String getRedisHost() {
        try {
            if (redisConnectionFactory != null) {
                String factoryInfo = redisConnectionFactory.toString();
                if (factoryInfo.contains("host=")) {
                    int startIndex = factoryInfo.indexOf("host=") + 5;
                    int endIndex = factoryInfo.indexOf(",", startIndex);
                    if (endIndex == -1) endIndex = factoryInfo.indexOf("}", startIndex);
                    if (endIndex > startIndex) {
                        return factoryInfo.substring(startIndex, endIndex);
                    }
                }
                return "redis-configured";
            }
        } catch (Exception e) {
            log.trace("Redis 호스트 정보 조회 실패 - {}", e.getMessage());
        }
        return "not-configured";
    }

    private String generateKey(String identifier, String action) {
        return keyPrefix + action + ":" + identifier;
    }

    private static class RateLimitInfo {
        private volatile int count;
        private final LocalDateTime createdAt;

        public RateLimitInfo(int count, LocalDateTime createdAt) {
            this.count = count;
            this.createdAt = createdAt;
        }

        public synchronized void incrementCount() {
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