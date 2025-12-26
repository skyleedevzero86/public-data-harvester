package com.antock.api.member.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@ConditionalOnMissingBean(RedisRateLimitService.class)
public class InMemoryRateLimitService implements RateLimitServiceInterface, AutoCloseable {

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlockInfo> blockedIdentifiers = new ConcurrentHashMap<>();
    private final Set<String> whitelistedIdentifiers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, ViolationTracker> violationTrackers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Value("${custom.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${custom.security.rate-limit.burst-capacity:100}")
    private int burstCapacity;

    @Value("${custom.security.rate-limit.max-violations-before-block:5}")
    private int maxViolationsBeforeBlock;

    @Value("${custom.security.rate-limit.block-duration-minutes:30}")
    private long defaultBlockDurationMinutes;

    @Value("${custom.security.rate-limit.violation-window-minutes:60}")
    private long violationWindowMinutes;

    public InMemoryRateLimitService() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::cleanupExpiredBlocks, 5, 5, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::logSecurityMetrics, 10, 10, TimeUnit.MINUTES);
    }

    @Override
    public boolean isRedisAvailable() {
        return false;
    }

    @Override
    public void checkRateLimit(String identifier, String action) {
        if (isWhitelisted(identifier)) {
            log.debug("화이트리스트된 식별자 {} - rate limiting 건너뜀", identifier);
            return;
        }

        if (isIdentifierBlocked(identifier)) {
            BlockInfo blockInfo = blockedIdentifiers.get(identifier);
            String errorMsg = String.format("식별자 %s가 차단됨. 사유: %s, 만료시간: %s",
                    identifier, blockInfo.getReason(),
                    LocalDateTime.ofEpochSecond(blockInfo.getExpiryTime(), 0, java.time.ZoneOffset.UTC));
            log.warn(errorMsg);
            throw new SecurityException(errorMsg);
        }

        String key = generateKey(identifier, action);
        RateLimitInfo info = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo(0, LocalDateTime.now()));

        LocalDateTime now = LocalDateTime.now();

        if (info.getCreatedAt().plusMinutes(1).isBefore(now)) {
            info.reset(now);
        }

        if (info.getCount() >= requestsPerMinute) {
            trackViolation(identifier, action);

            if (shouldAutoBlock(identifier)) {
                blockIdentifier(identifier, "자동 차단: Rate limit 위반 횟수 초과", defaultBlockDurationMinutes);
                log.warn("식별자 {}가 자동으로 차단됨. 위반 횟수: {}", identifier, getViolationCount(identifier));
            }

            throw new SecurityException("Rate limit exceeded for identifier: " + identifier);
        }

        info.incrementCount();
    }

    @Override
    public int getCurrentCount(String identifier, String action) {
        String key = generateKey(identifier, action);
        RateLimitInfo info = rateLimitMap.get(key);
        return info != null ? info.getCount() : 0;
    }

    @Override
    public void resetLimit(String identifier, String action) {
        String key = generateKey(identifier, action);
        RateLimitInfo info = rateLimitMap.get(key);
        if (info != null) {
            info.reset(LocalDateTime.now());
            log.info("Rate limit reset for identifier: {} action: {}", identifier, action);
        }
    }

    @Override
    public void blockIdentifier(String identifier, String reason, long blockDurationMinutes) {
        long expiryTime = System.currentTimeMillis() / 1000 + (blockDurationMinutes * 60);
        BlockInfo blockInfo = new BlockInfo(reason, expiryTime, LocalDateTime.now());
        blockedIdentifiers.put(identifier, blockInfo);

        log.warn("식별자 {}가 차단됨. 사유: {}, 만료시간: {}",
                identifier, reason,
                LocalDateTime.ofEpochSecond(expiryTime, 0, java.time.ZoneOffset.UTC));
    }

    @Override
    public void unblockIdentifier(String identifier) {
        BlockInfo removed = blockedIdentifiers.remove(identifier);
        if (removed != null) {
            log.info("식별자 {}의 차단이 해제됨", identifier);
        }
    }

    @Override
    public boolean isIdentifierBlocked(String identifier) {
        BlockInfo blockInfo = blockedIdentifiers.get(identifier);
        if (blockInfo == null) {
            return false;
        }

        if (System.currentTimeMillis() / 1000 > blockInfo.getExpiryTime()) {
            blockedIdentifiers.remove(identifier);
            return false;
        }

        return true;
    }

    @Override
    public void addToWhitelist(String identifier) {
        whitelistedIdentifiers.add(identifier);
        log.info("식별자 {}가 화이트리스트에 추가됨", identifier);
    }

    @Override
    public void removeFromWhitelist(String identifier) {
        boolean removed = whitelistedIdentifiers.remove(identifier);
        if (removed) {
            log.info("식별자 {}가 화이트리스트에서 제거됨", identifier);
        }
    }

    @Override
    public boolean isWhitelisted(String identifier) {
        return whitelistedIdentifiers.contains(identifier);
    }

    @Override
    public RateLimitSecurityInfo getSecurityInfo(String identifier) {
        BlockInfo blockInfo = blockedIdentifiers.get(identifier);
        ViolationTracker tracker = violationTrackers.get(identifier);

        boolean isBlocked = isIdentifierBlocked(identifier);
        String blockReason = blockInfo != null ? blockInfo.getReason() : null;
        long blockExpiryTime = blockInfo != null ? blockInfo.getExpiryTime() : 0;
        boolean isWhitelisted = isWhitelisted(identifier);
        int violationCount = tracker != null ? tracker.getViolationCount() : 0;
        long lastViolationTime = tracker != null ? tracker.getLastViolationTime() : 0;

        return new RateLimitSecurityInfo(isBlocked, blockReason, blockExpiryTime,
                isWhitelisted, violationCount, lastViolationTime);
    }

    private void trackViolation(String identifier, String action) {
        ViolationTracker tracker = violationTrackers.computeIfAbsent(identifier, k -> new ViolationTracker());
        tracker.recordViolation();

        log.warn("Rate limit 위반 기록: 식별자 {}, 액션 {}, 총 위반 횟수: {}",
                identifier, action, tracker.getViolationCount());
    }

    private boolean shouldAutoBlock(String identifier) {
        ViolationTracker tracker = violationTrackers.get(identifier);
        if (tracker == null) {
            return false;
        }

        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        int violationsInLastHour = tracker.getViolationsInWindow(oneHourAgo);

        return violationsInLastHour >= maxViolationsBeforeBlock;
    }

    private int getViolationCount(String identifier) {
        ViolationTracker tracker = violationTrackers.get(identifier);
        return tracker != null ? tracker.getViolationCount() : 0;
    }

    private void cleanupExpiredEntries() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
            rateLimitMap.entrySet().removeIf(entry ->
                    entry.getValue().getCreatedAt().isBefore(cutoff));
        } catch (Exception e) {
            log.error("만료된 rate limit 항목 정리 중 오류 발생", e);
        }
    }

    private void cleanupExpiredBlocks() {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            blockedIdentifiers.entrySet().removeIf(entry ->
                    entry.getValue().getExpiryTime() < currentTime);
        } catch (Exception e) {
            log.error("만료된 차단 항목 정리 중 오류 발생", e);
        }
    }

    private void logSecurityMetrics() {
        try {
            int totalBlocked = blockedIdentifiers.size();
            int totalWhitelisted = whitelistedIdentifiers.size();
            int totalViolationTrackers = violationTrackers.size();

            log.info("보안 메트릭 - 차단된 식별자: {}, 화이트리스트: {}, 위반 추적기: {}",
                    totalBlocked, totalWhitelisted, totalViolationTrackers);
        } catch (Exception e) {
            log.error("보안 메트릭 로깅 중 오류 발생", e);
        }
    }

    private String generateKey(String identifier, String action) {
        return identifier + ":" + action;
    }

    @Override
    public void close() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class RateLimitInfo {
        private final AtomicInteger count;
        private final AtomicReference<LocalDateTime> createdAt;

        RateLimitInfo(int count, LocalDateTime createdAt) {
            this.count = new AtomicInteger(count);
            this.createdAt = new AtomicReference<>(createdAt);
        }

        void incrementCount() {
            count.incrementAndGet();
        }

        int getCount() {
            return count.get();
        }

        LocalDateTime getCreatedAt() {
            return createdAt.get();
        }

        void reset(LocalDateTime newTime) {
            count.set(0);
            createdAt.set(newTime);
        }
    }

    private static class BlockInfo {
        private final String reason;
        private final long expiryTime;
        private final LocalDateTime blockedAt;

        BlockInfo(String reason, long expiryTime, LocalDateTime blockedAt) {
            this.reason = reason;
            this.expiryTime = expiryTime;
            this.blockedAt = blockedAt;
        }

        String getReason() { return reason; }
        long getExpiryTime() { return expiryTime; }
        LocalDateTime getBlockedAt() { return blockedAt; }
    }

    private static class ViolationTracker {
        private final AtomicInteger violationCount = new AtomicInteger(0);
        private final AtomicLong lastViolationTime = new AtomicLong(0);
        private final ConcurrentHashMap<Long, Integer> violationHistory = new ConcurrentHashMap<>();

        void recordViolation() {
            long now = System.currentTimeMillis();
            violationCount.incrementAndGet();
            lastViolationTime.set(now);

            long minuteKey = now / (60 * 1000);
            violationHistory.compute(minuteKey, (k, v) -> (v == null) ? 1 : v + 1);

            long cutoff = (now - (60 * 60 * 1000)) / (60 * 1000);
            violationHistory.entrySet().removeIf(entry -> entry.getKey() < cutoff);
        }

        int getViolationCount() {
            return violationCount.get();
        }

        long getLastViolationTime() {
            return lastViolationTime.get();
        }

        int getViolationsInWindow(long windowStart) {
            long windowStartMinute = windowStart / (60 * 1000);
            return violationHistory.entrySet().stream()
                    .filter(entry -> entry.getKey() >= windowStartMinute)
                    .mapToInt(Map.Entry::getValue)
                    .sum();
        }
    }
}