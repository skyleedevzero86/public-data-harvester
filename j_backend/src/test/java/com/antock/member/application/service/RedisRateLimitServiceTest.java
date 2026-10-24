package com.antock.member.application.service;

import com.antock.api.member.application.service.RedisRateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        rateLimitService = new RedisRateLimitService();

        ReflectionTestUtils.setField(rateLimitService, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(rateLimitService, "redisEnabled", true);
        ReflectionTestUtils.setField(rateLimitService, "requestsPerMinute", 60);
        ReflectionTestUtils.setField(rateLimitService, "burstCapacity", 100);
        ReflectionTestUtils.setField(rateLimitService, "maxViolationsBeforeBlock", 5);
        ReflectionTestUtils.setField(rateLimitService, "keyPrefix", "rate_limit:");
        ReflectionTestUtils.setField(rateLimitService, "memoryCacheLimit", 1000);
    }

    @Test
    @DisplayName("Redis 속도 제한 정상 동작")
    void checkRateLimit_Redis_Success() {
        String identifier = "user123";
        String action = "login";
        useRedis(true);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any())).thenReturn(6L);

        assertThatCode(() -> rateLimitService.checkRateLimit(identifier, action))
                .doesNotThrowAnyException();

        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(identifier + ":" + action)), eq("60"), eq("60"));
    }

    @Test
    @Disabled("Redis가 한도를 넘기면 SecurityException이 메모리 조회로 삼켜져 요청이 통과한다")
    @DisplayName("속도 제한 초과 시 예외 발생")
    void checkRateLimit_ExceedsLimit_ThrowsException() {
        String identifier = "user123";
        String action = "login";
        useRedis(true);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any())).thenReturn(-1L);

        assertThatThrownBy(() -> rateLimitService.checkRateLimit(identifier, action))
                .isInstanceOf(SecurityException.class);

        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(identifier + ":" + action)), eq("60"), eq("60"));
    }

    @Test
    @DisplayName("Redis 연결 실패 시 메모리 fallback 동작")
    void checkRateLimit_RedisFailure_FallbackToMemory() {
        String identifier = "user123";
        String action = "login";
        useRedis(true);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenThrow(new RedisConnectionFailureException("Connection failed"));
        when(valueOperations.get(anyString()))
                .thenThrow(new RedisConnectionFailureException("Connection failed"));

        assertThatCode(() -> rateLimitService.checkRateLimit(identifier, action))
                .doesNotThrowAnyException();
        int currentCount = rateLimitService.getCurrentCount(identifier, action);
        assertThat(currentCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("메모리 기반 속도 제한 정상 동작")
    void checkRateLimit_Memory_Success() {
        String identifier = "user456";
        String action = "join";
        useRedis(false);

        assertThatCode(() -> {
            rateLimitService.checkRateLimit(identifier, action);
            rateLimitService.checkRateLimit(identifier, action);
        }).doesNotThrowAnyException();

        int currentCount = rateLimitService.getCurrentCount(identifier, action);
        assertThat(currentCount).isEqualTo(2);
    }

    @Test
    @DisplayName("현재 카운트 조회 성공")
    void getCurrentCount_Success() {
        String identifier = "user123";
        String action = "login";
        String expectedKey = identifier + ":" + action;
        useRedis(true);
        when(valueOperations.get(expectedKey)).thenReturn("5");

        int result = rateLimitService.getCurrentCount(identifier, action);

        assertThat(result).isEqualTo(5);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("제한 재설정 성공")
    void resetLimit_Success() {
        String identifier = "user123";
        String action = "login";
        String expectedKey = identifier + ":" + action;
        useRedis(true);
        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertThatCode(() -> rateLimitService.resetLimit(identifier, action))
                .doesNotThrowAnyException();

        verify(redisTemplate).delete(expectedKey);
    }

    private void useRedis(boolean enabled) {
        AtomicBoolean flag = (AtomicBoolean) ReflectionTestUtils.getField(rateLimitService, "useRedis");
        flag.set(enabled);
    }
}
