package com.antock.member.application.service;

import com.antock.api.member.application.service.RedisRateLimitService;
import com.antock.global.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        rateLimitService = new RedisRateLimitService();

        ReflectionTestUtils.setField(rateLimitService, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(rateLimitService, "redisEnabled", true);
        ReflectionTestUtils.setField(rateLimitService, "burstCapacity", 100);
        ReflectionTestUtils.setField(rateLimitService, "keyPrefix", "rate_limit:");
        ReflectionTestUtils.setField(rateLimitService, "memoryCacheLimit", 1000);
    }

    @Test
    @DisplayName("Redis 속도 제한 정상 동작")
    void checkRateLimit_Redis_Success() {
        // given
        String identifier = "user123";
        String action = "login";
        String expectedKey = "rate_limit:login:user123";

        when(valueOperations.get(expectedKey)).thenReturn("5");
        when(valueOperations.increment(expectedKey)).thenReturn(6L);
        when(redisTemplate.expire(eq(expectedKey), any())).thenReturn(true);

        // when & then
        assertThatCode(() -> rateLimitService.checkRateLimit(identifier, action))
                .doesNotThrowAnyException();

        verify(valueOperations).get(expectedKey);
        verify(valueOperations).increment(expectedKey);
    }

    @Test
    @DisplayName("속도 제한 초과 시 예외 발생")
    void checkRateLimit_ExceedsLimit_ThrowsException() {
        // given
        String identifier = "user123";
        String action = "login";
        String expectedKey = "rate_limit:login:user123";

        when(valueOperations.get(expectedKey)).thenReturn("100"); // 제한값과 동일

        // when & then
        assertThatThrownBy(() -> rateLimitService.checkRateLimit(identifier, action))
                .isInstanceOf(BusinessException.class);

        verify(valueOperations).get(expectedKey);
        verify(valueOperations, never()).increment(expectedKey);
    }

    @Test
    @DisplayName("Redis 연결 실패 시 메모리 fallback 동작")
    void checkRateLimit_RedisFailure_FallbackToMemory() {
        // given
        String identifier = "user123";
        String action = "login";

        when(valueOperations.get(anyString()))
                .thenThrow(new org.springframework.data.redis.RedisConnectionFailureException("Connection failed"));

        // when & then
        assertThatCode(() -> rateLimitService.checkRateLimit(identifier, action))
                .doesNotThrowAnyException();
        int currentCount = rateLimitService.getCurrentCount(identifier, action);
        assertThat(currentCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("메모리 기반 속도 제한 정상 동작")
    void checkRateLimit_Memory_Success() {
        // given
        String identifier = "user456";
        String action = "join";

        ReflectionTestUtils.setField(rateLimitService, "redisEnabled", false);

        // when & then
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
        // given
        String identifier = "user123";
        String action = "login";
        String expectedKey = "rate_limit:login:user123";

        when(valueOperations.get(expectedKey)).thenReturn("5");

        // when
        int result = rateLimitService.getCurrentCount(identifier, action);

        // then
        assertThat(result).isEqualTo(5);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("제한 재설정 성공")
    void resetLimit_Success() {
        // given
        String identifier = "user123";
        String action = "login";
        String expectedKey = "rate_limit:login:user123";

        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        // when
        assertThatCode(() -> rateLimitService.resetLimit(identifier, action))
                .doesNotThrowAnyException();

        // then
        verify(redisTemplate).delete(expectedKey);
    }
}