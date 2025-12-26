package com.antock.api.member.application.service.strategy;

import com.antock.api.member.application.service.RateLimitSecurityInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class MemoryRateLimitStrategy implements RateLimitStrategy {

    @Value("${custom.security.rate-limit.memory-cache-limit:1000}")
    private int memoryCacheLimit;

    private final ConcurrentHashMap<String, RateLimitInfo> memoryCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlockInfo> blockedIdentifiers = new ConcurrentHashMap<>();
    private final Set<String> whitelistedIdentifiers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, ViolationTracker> violationTrackers = new ConcurrentHashMap<>();

    @Override
    public void checkRateLimit(String identifier, String action, int requestsPerMinute) {
        String key = generateKey(identifier, action);
        RateLimitInfo info = memoryCache.computeIfAbsent(key, k -> new RateLimitInfo(0, LocalDateTime.now()));

        LocalDateTime now = LocalDateTime.now();

        if (info.getCreatedAt().plusMinutes(1).isBefore(now)) {
            info.reset(now);
        }

        if (info.getCount() >= requestsPerMinute) {
            throw new SecurityException("Rate limit exceeded for identifier: " + identifier);
        }

        info.incrementCount();
    }

    @Override
    public int getCurrentCount(String identifier, String action) {
        String key = generateKey(identifier, action);
        RateLimitInfo info = memoryCache.get(key);
        return info != null ? info.getCount() : 0;
    }

    @Override
    public void resetLimit(String identifier, String action) {
        String key = generateKey(identifier, action);
        RateLimitInfo info = memoryCache.get(key);
        if (info != null) {
            info.reset(LocalDateTime.now());
            log.info("Rate limit reset for identifier: {} action: {} (memory)", identifier, action);
        }
    }

    @Override
    public void blockIdentifier(String identifier, String reason, long blockDurationMinutes) {
        long expiryTime = System.currentTimeMillis() / 1000 + (blockDurationMinutes * 60);
        BlockInfo blockInfo = new BlockInfo(reason, expiryTime, LocalDateTime.now());
        blockedIdentifiers.put(identifier, blockInfo);

        cleanupIdentifierData(identifier);
        log.warn("식별자 {}가 메모리에서 차단됨. 사유: {}, 만료시간: {}",
                identifier, reason,
                LocalDateTime.ofEpochSecond(expiryTime, 0, java.time.ZoneOffset.UTC));
    }

    @Override
    public void unblockIdentifier(String identifier) {
        BlockInfo removed = blockedIdentifiers.remove(identifier);
        if (removed != null) {
            log.info("식별자 {}의 차단이 메모리에서 해제됨", identifier);
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
        log.info("식별자 {}가 메모리 화이트리스트에 추가됨", identifier);
    }

    @Override
    public void removeFromWhitelist(String identifier) {
        whitelistedIdentifiers.remove(identifier);
        log.info("식별자 {}가 메모리 화이트리스트에서 제거됨", identifier);
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
        String blockReason = isBlocked ? blockInfo.getReason() : null;
        long blockExpiryTime = isBlocked ? blockInfo.getExpiryTime() : 0;
        boolean isWhitelisted = isWhitelisted(identifier);
        int violationCount = tracker != null ? tracker.getViolationCount() : 0;
        long lastViolationTime = tracker != null ? tracker.getLastViolationTime() : 0;

        return new RateLimitSecurityInfo(isBlocked, blockReason, blockExpiryTime,
                isWhitelisted, violationCount, lastViolationTime);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public void trackViolation(String identifier, String action) {
        ViolationTracker tracker = violationTrackers.computeIfAbsent(identifier,
                k -> new ViolationTracker());
        tracker.recordViolation();

        log.warn("Rate limit 위반 기록: 식별자 {}, 액션 {}, 총 위반 횟수: {}",
                identifier, action, tracker.getViolationCount());
    }

    public boolean shouldAutoBlock(String identifier, int maxViolationsBeforeBlock, long violationWindowMinutes) {
        ViolationTracker tracker = violationTrackers.get(identifier);
        if (tracker == null) {
            return false;
        }

        long windowStart = System.currentTimeMillis() - (violationWindowMinutes * 60 * 1000);
        int violationsInWindow = tracker.getViolationsInWindow(windowStart);

        return violationsInWindow >= maxViolationsBeforeBlock;
    }

    public int getViolationCount(String identifier) {
        ViolationTracker tracker = violationTrackers.get(identifier);
        return tracker != null ? tracker.getViolationCount() : 0;
    }

    public void cleanupMemoryCache() {
        int beforeSize = memoryCache.size();
        memoryCache.clear();
        blockedIdentifiers.clear();
        whitelistedIdentifiers.clear();
        violationTrackers.clear();
        log.info("메모리 캐시 정리 완료: {} -> 0", beforeSize);
    }

    private void cleanupIdentifierData(String identifier) {
        memoryCache.entrySet().removeIf(entry -> entry.getKey().startsWith(identifier + ":"));
        violationTrackers.remove(identifier);
    }

    private String generateKey(String identifier, String action) {
        return identifier + ":" + action;
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

