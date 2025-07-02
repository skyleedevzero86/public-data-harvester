package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.domain.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("dev")
@ConditionalOnProperty(name = "custom.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisMemberCacheService implements MemberCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisMemberCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${custom.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${custom.redis.member-cache-prefix:member:}")
    private String memberCachePrefix;

    @Value("${custom.redis.member-cache-ttl-minutes:5}")
    private int cacheTtlMinutes;

    @Value("${custom.redis.member-profile-prefix:profile:}")
    private String profileCachePrefix;

    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheErrors = new AtomicLong(0);
    private boolean cacheAvailable = false;

    @Autowired
    public RedisMemberCacheService(@Qualifier("antockRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                   ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        cacheAvailable = redisEnabled && redisTemplate != null && checkCacheConnection();
        if (cacheAvailable) {
            log.info("RedisMemberCacheService 초기화 완료 - TTL: {}분", cacheTtlMinutes);
        } else {
            log.warn("RedisMemberCacheService 캐시 비활성화 - 직접 DB 조회로 동작");
        }
    }

    @Override
    public MemberResponse getMemberFromCache(Long memberId) {
        if (!cacheAvailable) {
            return null;
        }
        try {
            String key = generateMemberKey(memberId);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                cacheHits.incrementAndGet();
                MemberResponse memberResponse = convertToMemberResponse(cached);
                log.debug("회원 정보 캐시 히트 - ID: {}", memberId);
                return memberResponse;
            } else {
                cacheMisses.incrementAndGet();
                log.debug("회원 정보 캐시 미스 - ID: {}", memberId);
                return null;
            }
        } catch (RedisConnectionFailureException e) {
            handleCacheError("Redis 연결 실패", e);
            return null;
        } catch (RedisSystemException e) {
            handleCacheError("Redis 시스템 오류", e);
            return null;
        } catch (Exception e) {
            handleCacheError("캐시 조회 오류", e);
            return null;
        }
    }

    @Override
    public void cacheMember(Member member) {
        if (!cacheAvailable) {
            return;
        }
        try {
            String key = generateMemberKey(member.getId());
            MemberResponse memberResponse = MemberResponse.from(member);
            redisTemplate.opsForValue().set(key, memberResponse, Duration.ofMinutes(cacheTtlMinutes));
            log.debug("회원 정보 캐시 저장 - ID: {}, TTL: {}분", member.getId(), cacheTtlMinutes);
        } catch (Exception e) {
            handleCacheError("캐시 저장 오류", e);
        }
    }

    @Override
    public void cacheMemberResponse(MemberResponse memberResponse) {
        if (!cacheAvailable || memberResponse == null) {
            return;
        }
        try {
            String key = generateMemberKey(memberResponse.getId());
            redisTemplate.opsForValue().set(key, memberResponse, Duration.ofMinutes(cacheTtlMinutes));
            log.debug("회원 응답 캐시 저장 - ID: {}, username: {}", memberResponse.getId(), memberResponse.getUsername());
        } catch (Exception e) {
            handleCacheError("응답 캐시 저장 오류", e);
        }
    }

    @Override
    public MemberResponse getMemberProfileFromCache(Long memberId) {
        if (!cacheAvailable) {
            return null;
        }
        try {
            String key = generateProfileKey(memberId);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                cacheHits.incrementAndGet();
                MemberResponse profileResponse = convertToMemberResponse(cached);
                log.debug("회원 프로필 캐시 히트 - ID: {}", memberId);
                return profileResponse;
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
        if (!cacheAvailable || memberResponse == null) {
            return;
        }
        try {
            String key = generateProfileKey(memberResponse.getId());
            redisTemplate.opsForValue().set(key, memberResponse, Duration.ofMinutes(cacheTtlMinutes));
            log.debug("회원 프로필 캐시 저장 - ID: {}", memberResponse.getId());
        } catch (Exception e) {
            handleCacheError("프로필 캐시 저장 오류", e);
        }
    }

    @Override
    public void evictMemberCache(Long memberId) {
        if (!cacheAvailable) {
            return;
        }
        try {
            String memberKey = generateMemberKey(memberId);
            String profileKey = generateProfileKey(memberId);
            Boolean memberDeleted = redisTemplate.delete(memberKey);
            Boolean profileDeleted = redisTemplate.delete(profileKey);
            log.info("회원 캐시 무효화 - ID: {}, 회원정보: {}, 프로필: {}", memberId, memberDeleted, profileDeleted);
        } catch (Exception e) {
            handleCacheError("캐시 무효화 오류", e);
        }
    }

    @Override
    public void evictAllMemberCache() {
        if (!cacheAvailable) {
            return;
        }
        try {
            Set<String> memberKeys = redisTemplate.keys(memberCachePrefix + "*");
            Set<String> profileKeys = redisTemplate.keys(profileCachePrefix + "*");
            long memberDeleted = 0;
            long profileDeleted = 0;
            if (memberKeys != null && !memberKeys.isEmpty()) {
                memberDeleted = redisTemplate.delete(memberKeys);
            }
            if (profileKeys != null && !profileKeys.isEmpty()) {
                profileDeleted = redisTemplate.delete(profileKeys);
            }
            log.warn("전체 회원 캐시 무효화 - 회원정보: {}, 프로필: {}", memberDeleted, profileDeleted);
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

    @Scheduled(fixedDelay = 60000)
    public void checkCacheHealth() {
        if (!redisEnabled) {
            return;
        }
        boolean wasAvailable = cacheAvailable;
        cacheAvailable = checkCacheConnection();
        if (!wasAvailable && cacheAvailable) {
            log.info("Redis 캐시 연결 복구됨");
        } else if (wasAvailable && !cacheAvailable) {
            log.warn("Redis 캐시 연결 실패 감지");
        }
    }

    @Scheduled(fixedDelay = 600000)
    public void logCacheStatistics() {
        if (!cacheAvailable) {
            return;
        }
        CacheStatistics stats = getCacheStatistics();
        if (stats.getTotalRequests() > 0) {
            log.info("Redis 캐시 통계 - 히트율: {:.1f}%, 히트: {}, 미스: {}, 오류: {}",
                    stats.getHitRate(), stats.getCacheHits(),
                    stats.getCacheMisses(), stats.getCacheErrors());
        }
    }

    private boolean checkCacheConnection() {
        if (redisTemplate == null) {
            return false;
        }
        try {
            String testKey = "member:health:check";
            redisTemplate.opsForValue().set(testKey, "ok", Duration.ofSeconds(5));
            Object result = redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            return "ok".equals(String.valueOf(result));
        } catch (Exception e) {
            log.debug("Redis 캐시 헬스체크 실패 - {}", e.getMessage());
            return false;
        }
    }

    private void handleCacheError(String operation, Exception e) {
        cacheErrors.incrementAndGet();
        log.error("Redis 캐시 {} - {}", operation, e.getMessage());
        if (cacheErrors.get() % 10 == 0) {
            log.warn("Redis 캐시 오류 빈발 - 총 {}회", cacheErrors.get());
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