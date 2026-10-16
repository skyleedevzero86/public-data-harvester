package com.antock.api.admin.health;

import com.antock.api.member.application.service.MemoryMemberCacheService;
import com.antock.api.member.application.service.RateLimitService;
import com.antock.api.member.application.service.RedisRateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryFallbackRecoveryListener {

    private final ObjectProvider<RateLimitService> rateLimitService;
    private final ObjectProvider<RedisRateLimitService> redisRateLimitService;
    private final ObjectProvider<MemoryMemberCacheService> memoryMemberCacheService;

    @EventListener
    public void onRedisRecovered(RedisRecoveredEvent event) {
        rateLimitService.ifAvailable(RateLimitService::resumeAfterRedisRecovery);
        redisRateLimitService.ifAvailable(RedisRateLimitService::resumeAfterRedisRecovery);
        memoryMemberCacheService.ifAvailable(MemoryMemberCacheService::evictAllMemberCache);
        log.info("Redis 복구로 메모리 fallback 캐시를 비웠습니다.");
    }
}
