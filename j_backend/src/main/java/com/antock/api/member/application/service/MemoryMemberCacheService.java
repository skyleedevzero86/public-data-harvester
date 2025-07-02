package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.domain.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("prod")
public class MemoryMemberCacheService implements MemberCacheService {

    private static final Logger log = LoggerFactory.getLogger(MemoryMemberCacheService.class);

    private static class CacheEntry {
        final MemberResponse value;
        final long expiryTime;

        CacheEntry(MemberResponse value, long ttlMinutes) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMinutes * 60 * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Value("${custom.redis.member-cache-prefix:member:}")
    private String memberCachePrefix;

    @Value("${custom.redis.member-cache-ttl-minutes:5}")
    private int cacheTtlMinutes;

    @Value("${custom.redis.member-profile-prefix:profile:}")
    private String profileCachePrefix;

    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheErrors = new AtomicLong(0);
    private boolean cacheAvailable = true;

    public MemoryMemberCacheService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        log.info("MemoryMemberCacheService 초기화 완료 - TTL: {}분", cacheTtlMinutes);
    }

    @Override
    public MemberResponse getMemberFromCache(Long memberId) {
        try {
            String key = generateMemberKey(memberId);
            CacheEntry entry = cache.get(key);

            if (entry != null) {
                if (!entry.isExpired()) {
                    cacheHits.incrementAndGet();
                    log.debug("회원 정보 캐시 히트 - ID: {}", memberId);
                    return entry.value;
                } else {
                    cache.remove(key);
                    cacheMisses.incrementAndGet();
                    log.debug("회원 정보 캐시 만료 - ID: {}", memberId);
                    return null;
                }
            } else {
                cacheMisses.incrementAndGet();
                log.debug("회원 정보 캐시 미스 - ID: {}", memberId);
                return null;
            }
        } catch (Exception e) {
            handleCacheError("캐시 조회 오류", e);
            return null;
        }
    }

    @Override
    public void cacheMember(Member member) {
        try {
            String key = generateMemberKey(member.getId());
            MemberResponse memberResponse = MemberResponse.from(member);
            cache.put(key, new CacheEntry(memberResponse, cacheTtlMinutes));
            log.debug("회원 정보 캐시 저장 - ID: {}, TTL: {}분", member.getId(), cacheTtlMinutes);
        } catch (Exception e) {
            handleCacheError("캐시 저장 오류", e);
        }
    }

    @Override
    public void cacheMemberResponse(MemberResponse memberResponse) {
        if (memberResponse == null) {
            return;
        }
        try {
            String key = generateMemberKey(memberResponse.getId());
            cache.put(key, new CacheEntry(memberResponse, cacheTtlMinutes));
            log.debug("회원 응답 캐시 저장 - ID: {}, username: {}", memberResponse.getId(), memberResponse.getUsername());
        } catch (Exception e) {
            handleCacheError("응답 캐시 저장 오류", e);
        }
    }

    @Override
    public MemberResponse getMemberProfileFromCache(Long memberId) {
        try {
            String key = generateProfileKey(memberId);
            CacheEntry entry = cache.get(key);

            if (entry != null) {
                if (!entry.isExpired()) {
                    cacheHits.incrementAndGet();
                    log.debug("회원 프로필 캐시 히트 - ID: {}", memberId);
                    return entry.value;
                } else {
                    cache.remove(key);
                    cacheMisses.incrementAndGet();
                    log.debug("회원 프로필 캐시 만료 - ID: {}", memberId);
                    return null;
                }
            } else {
                cacheMisses.incrementAndGet();
                log.debug("회원 프로필 캐시 미스 - ID: {}", memberId);
                return null;
            }
        } catch (Exception e) {
            handleCacheError("프로필 캐시 조회 오류", e);
            return null;
        }
    }

    @Override
    public void cacheMemberProfile(MemberResponse memberResponse) {
        if (memberResponse == null) {
            return;
        }
        try {
            String key = generateProfileKey(memberResponse.getId());
            cache.put(key, new CacheEntry(memberResponse, cacheTtlMinutes));
            log.debug("회원 프로필 캐시 저장 - ID: {}", memberResponse.getId());
        } catch (Exception e) {
            handleCacheError("프로필 캐시 저장 오류", e);
        }
    }

    @Override
    public void evictMemberCache(Long memberId) {
        try {
            String memberKey = generateMemberKey(memberId);
            String profileKey = generateProfileKey(memberId);

            boolean memberDeleted = cache.remove(memberKey) != null;
            boolean profileDeleted = cache.remove(profileKey) != null;

            log.info("회원 캐시 무효화 - ID: {}, 회원정보: {}, 프로필: {}", memberId, memberDeleted, profileDeleted);
        } catch (Exception e) {
            handleCacheError("캐시 무효화 오류", e);
        }
    }

    @Override
    public void evictAllMemberCache() {
        try {
            cache.clear();
            log.warn("전체 회원 캐시 무효화");
        } catch (Exception e) {
            handleCacheError("전체 캐시 무효화 오류", e);
        }
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long errors = cacheErrors.get();
        long total = hits + misses;

        double hitRate = total > 0 ? (double) hits / total * 100 : 0.0;

        return new CacheStatistics(hits, misses, errors, hitRate, total, cacheAvailable);
    }

    @Scheduled(fixedDelay = 600000)
    public void logCacheStatistics() {
        if (!cacheAvailable) {
            return;
        }
        CacheStatistics stats = getCacheStatistics();
        if (stats.getTotalRequests() > 0) {
            log.info("Memory 캐시 통계 - 히트율: {:.1f}%, 히트: {}, 미스: {}, 오류: {}",
                    stats.getHitRate(), stats.getCacheHits(),
                    stats.getCacheMisses(), stats.getCacheErrors());
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void evictExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.debug("만료된 캐시 항목 제거 완료");
    }

    private void handleCacheError(String operation, Exception e) {
        cacheErrors.incrementAndGet();
        log.error("Memory 캐시 {} - {}", operation, e.getMessage());
        if (cacheErrors.get() % 10 == 0) {
            log.warn("Memory 캐시 오류 빈발 - 총 {}회", cacheErrors.get());
        }
    }

    private MemberResponse convertToMemberResponse(Object cached) {
        if (cached instanceof MemberResponse) {
            return (MemberResponse) cached;
        }
        if (cached instanceof String) {
            try {
                return objectMapper.readValue((String) cached, MemberResponse.class);
            } catch (JsonProcessingException e) {
                log.error("JSON 역직렬화 실패 - {}", e.getMessage());
                throw new RuntimeException("캐시 데이터 변환 실패", e);
            }
        }
        throw new IllegalArgumentException("지원하지 않는 캐시 데이터 타입: " + cached.getClass());
    }

    private String generateMemberKey(Long memberId) {
        return memberCachePrefix + memberId;
    }

    private String generateProfileKey(Long memberId) {
        return profileCachePrefix + memberId;
    }
}