package com.antock.api.member.infrastructure.security.service;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${custom.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${custom.security.rate-limit.burst-capacity:100}")
    private int burstCapacity;

    @Value("${custom.redis.rate-limit-key-prefix}")
    private String keyPrefix;

    public void checkRateLimit(String identifier, String action) {
        String key = generateKey(identifier, action);
        String countStr = redisTemplate.opsForValue().get(key);

        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

        if (currentCount >= burstCapacity) {
            log.warn("Rate limit exceeded for identifier: {}, action: {}, count: {}",
                    identifier, action, currentCount);
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

        // 카운트 증가
        redisTemplate.opsForValue().increment(key);

        // 첫 번째 요청인 경우 TTL 설정
        if (currentCount == 0) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
    }

    public int getCurrentCount(String identifier, String action) {
        String key = generateKey(identifier, action);
        String countStr = redisTemplate.opsForValue().get(key);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    public void resetLimit(String identifier, String action) {
        String key = generateKey(identifier, action);
        redisTemplate.delete(key);
    }

    private String generateKey(String identifier, String action) {
        return keyPrefix + action + ":" + identifier;
    }
}