package com.antock.api.member.application.service;

import com.antock.api.member.application.service.strategy.MemoryRateLimitStrategy;
import com.antock.api.member.application.service.strategy.RateLimitStrategy;
import com.antock.api.member.application.service.strategy.RedisRateLimitStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class RateLimitService implements RateLimitServiceInterface {

    private final RedisRateLimitStrategy redisStrategy;
    private final MemoryRateLimitStrategy memoryStrategy;

    @Value("${custom.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${custom.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${custom.security.rate-limit.max-violations-before-block:5}")
    private int maxViolationsBeforeBlock;

    @Value("${custom.security.rate-limit.block-duration-minutes:30}")
    private long defaultBlockDurationMinutes;

    @Value("${custom.security.rate-limit.violation-window-minutes:60}")
    private long violationWindowMinutes;

    @Value("${custom.security.rate-limit.memory-cache-limit:1000}")
    private int memoryCacheLimit;

    private final AtomicBoolean useRedis = new AtomicBoolean(false);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    @PostConstruct
    public void init() {
        if (redisEnabled && redisStrategy.isAvailable()) {
            useRedis.set(true);
            consecutiveFailures.set(0);
            log.info("Redis 연결 성공 - Redis 기반 rate limiting을 사용합니다.");
        } else {
            useRedis.set(false);
            log.info("Redis를 사용할 수 없습니다. 메모리 기반 rate limiting을 사용합니다.");
        }
    }

    @Override
    public boolean isRedisAvailable() {
        return useRedis.get() && redisStrategy.isAvailable();
    }

    @Override
    public void checkRateLimit(String identifier, String action) {
        if (getCurrentStrategy().isWhitelisted(identifier)) {
            log.debug("화이트리스트된 식별자 {} - rate limiting 건너뜀", identifier);
            return;
        }

        if (getCurrentStrategy().isIdentifierBlocked(identifier)) {
            RateLimitSecurityInfo securityInfo = getCurrentStrategy().getSecurityInfo(identifier);
            String errorMsg = String.format("식별자 %s가 차단됨. 사유: %s",
                    identifier, securityInfo.getBlockReason());
            log.warn(errorMsg);
            throw new SecurityException(errorMsg);
        }

        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            strategy.checkRateLimit(identifier, action, requestsPerMinute);
        } catch (SecurityException e) {
            handleRateLimitExceeded(identifier, action, strategy);
            throw e;
        } catch (Exception e) {
            handleStrategyFailure("Rate limit 확인 실패", identifier, action, e);
            getFallbackStrategy().checkRateLimit(identifier, action, requestsPerMinute);
        }
    }

    @Override
    public int getCurrentCount(String identifier, String action) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            return strategy.getCurrentCount(identifier, action);
        } catch (Exception e) {
            log.warn("전략에서 카운트 조회 실패, fallback 사용: {}", e.getMessage());
            return getFallbackStrategy().getCurrentCount(identifier, action);
        }
    }

    @Override
    public void resetLimit(String identifier, String action) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            strategy.resetLimit(identifier, action);
        } catch (Exception e) {
            log.warn("전략에서 limit reset 실패, fallback 사용: {}", e.getMessage());
            getFallbackStrategy().resetLimit(identifier, action);
        }
    }

    @Override
    public void blockIdentifier(String identifier, String reason, long blockDurationMinutes) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            strategy.blockIdentifier(identifier, reason, blockDurationMinutes);
        } catch (Exception e) {
            log.warn("전략에서 차단 처리 실패, fallback 사용: {}", e.getMessage());
            getFallbackStrategy().blockIdentifier(identifier, reason, blockDurationMinutes);
        }
    }

    @Override
    public void unblockIdentifier(String identifier) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            strategy.unblockIdentifier(identifier);
        } catch (Exception e) {
            log.warn("전략에서 차단 해제 실패, fallback 사용: {}", e.getMessage());
            getFallbackStrategy().unblockIdentifier(identifier);
        }
    }

    @Override
    public boolean isIdentifierBlocked(String identifier) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            return strategy.isIdentifierBlocked(identifier);
        } catch (Exception e) {
            log.warn("전략에서 차단 상태 확인 실패, fallback 사용: {}", e.getMessage());
            return getFallbackStrategy().isIdentifierBlocked(identifier);
        }
    }

    @Override
    public void addToWhitelist(String identifier) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            strategy.addToWhitelist(identifier);
        } catch (Exception e) {
            log.warn("전략에서 화이트리스트 추가 실패, fallback 사용: {}", e.getMessage());
            getFallbackStrategy().addToWhitelist(identifier);
        }
    }

    @Override
    public void removeFromWhitelist(String identifier) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            strategy.removeFromWhitelist(identifier);
        } catch (Exception e) {
            log.warn("전략에서 화이트리스트 제거 실패, fallback 사용: {}", e.getMessage());
            getFallbackStrategy().removeFromWhitelist(identifier);
        }
    }

    @Override
    public boolean isWhitelisted(String identifier) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            return strategy.isWhitelisted(identifier);
        } catch (Exception e) {
            log.warn("전략에서 화이트리스트 확인 실패, fallback 사용: {}", e.getMessage());
            return getFallbackStrategy().isWhitelisted(identifier);
        }
    }

    @Override
    public RateLimitSecurityInfo getSecurityInfo(String identifier) {
        RateLimitStrategy strategy = getCurrentStrategy();
        try {
            RateLimitSecurityInfo info = strategy.getSecurityInfo(identifier);
            return info;
        } catch (Exception e) {
            log.warn("전략에서 보안 정보 조회 실패, fallback 사용: {}", e.getMessage());
            return getFallbackStrategy().getSecurityInfo(identifier);
        }
    }

    private RateLimitStrategy getCurrentStrategy() {
        return useRedis.get() && redisStrategy.isAvailable() ? redisStrategy : memoryStrategy;
    }

    private RateLimitStrategy getFallbackStrategy() {
        return memoryStrategy;
    }

    private void handleRateLimitExceeded(String identifier, String action, RateLimitStrategy strategy) {
        if (strategy == memoryStrategy) {
            memoryStrategy.trackViolation(identifier, action);

            if (memoryStrategy.shouldAutoBlock(identifier, maxViolationsBeforeBlock, violationWindowMinutes)) {
                blockIdentifier(identifier, "자동 차단: Rate limit 위반 횟수 초과", defaultBlockDurationMinutes);
                log.warn("식별자 {}가 자동으로 차단됨. 위반 횟수: {}",
                        identifier, memoryStrategy.getViolationCount(identifier));
            }
        }
    }

    private void handleStrategyFailure(String message, String identifier, String action, Exception e) {
        if (useRedis.get()) {
            consecutiveFailures.incrementAndGet();
            log.error("{} - identifier: {}, action: {}, 오류: {}", message, identifier, action, e.getMessage());

            if (consecutiveFailures.get() >= MAX_CONSECUTIVE_FAILURES) {
                switchToMemoryFallback("연속적인 Redis 연결 실패");
            }
        }
    }

    private void switchToMemoryFallback(String reason) {
        useRedis.set(false);
        log.warn("Redis에서 메모리 fallback으로 전환: {}", reason);
        if (memoryCacheLimit > 0 && memoryStrategy instanceof MemoryRateLimitStrategy) {
            ((MemoryRateLimitStrategy) memoryStrategy).cleanupMemoryCache();
        }
    }
}

