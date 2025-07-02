package com.antock.member.service;

import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.MemberCacheService;
import com.antock.api.member.application.service.RedisMemberCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisMemberCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisMemberCacheService memberCacheService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // RedisMemberCacheService 초기화
        memberCacheService = new RedisMemberCacheService(redisTemplate, objectMapper);

        // @Value 속성 설정
        ReflectionTestUtils.setField(memberCacheService, "redisEnabled", true);
        ReflectionTestUtils.setField(memberCacheService, "memberCachePrefix", "member:");
        ReflectionTestUtils.setField(memberCacheService, "profileCachePrefix", "profile:");
        ReflectionTestUtils.setField(memberCacheService, "cacheTtlMinutes", 5);

        // @PostConstruct 호출 시뮬레이션
        ReflectionTestUtils.invokeMethod(memberCacheService, "init");
    }

    @Test
    @DisplayName("회원 정보 캐시 저장 및 조회 성공")
    void cacheMemberAndRetrieve_Success() {
        // given
        Long memberId = 1L;
        String expectedKey = "member:" + memberId;
        MemberResponse testResponse = createTestMemberResponse();

        when(valueOperations.get(expectedKey)).thenReturn(testResponse);

        // when
        memberCacheService.cacheMemberResponse(testResponse);
        MemberResponse result = memberCacheService.getMemberFromCache(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(valueOperations).set(eq(expectedKey), eq(testResponse), eq(Duration.ofMinutes(5)));
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("캐시 미스 시 null 반환")
    void getMemberFromCache_CacheMiss_ReturnsNull() {
        // given
        Long memberId = 999L;
        String expectedKey = "member:" + memberId;

        when(valueOperations.get(expectedKey)).thenReturn(null);

        // when
        MemberResponse result = memberCacheService.getMemberFromCache(memberId);

        // then
        assertThat(result).isNull();
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("Redis 연결 실패 시 예외 처리")
    void getMemberFromCache_RedisConnectionFailure_ReturnsNull() {
        // given
        Long memberId = 1L;
        String expectedKey = "member:" + memberId;

        when(valueOperations.get(expectedKey))
                .thenThrow(new org.springframework.data.redis.RedisConnectionFailureException("Connection failed"));

        // when
        MemberResponse result = memberCacheService.getMemberFromCache(memberId);

        // then
        assertThat(result).isNull();
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("캐시 무효화 성공")
    void evictMemberCache_Success() {
        // given
        Long memberId = 1L;
        String memberKey = "member:" + memberId;
        String profileKey = "profile:" + memberId;

        when(redisTemplate.delete(memberKey)).thenReturn(true);
        when(redisTemplate.delete(profileKey)).thenReturn(true);

        // when
        memberCacheService.evictMemberCache(memberId);

        // then
        verify(redisTemplate).delete(memberKey);
        verify(redisTemplate).delete(profileKey);
    }

    @Test
    @DisplayName("캐시 통계 정확성 검증")
    void getCacheStatistics_AccurateStatistics() {
        // given
        Long memberId = 1L;
        String expectedKey = "member:" + memberId;
        MemberResponse testResponse = createTestMemberResponse();

        // 캐시 히트 시뮬레이션
        when(valueOperations.get(expectedKey)).thenReturn(testResponse);
        memberCacheService.getMemberFromCache(memberId);

        // 캐시 미스 시뮬레이션
        when(valueOperations.get("member:999")).thenReturn(null);
        memberCacheService.getMemberFromCache(999L);

        // when
        MemberCacheService.CacheStatistics stats = memberCacheService.getCacheStatistics();

        // then
        assertThat(stats.getCacheHits()).isEqualTo(1);
        assertThat(stats.getCacheMisses()).isEqualTo(1);
        assertThat(stats.getTotalRequests()).isEqualTo(2);
        assertThat(stats.getHitRate()).isEqualTo(50.0);
    }

    private MemberResponse createTestMemberResponse() {
        return MemberResponse.builder()
                .id(1L)
                .username("testuser")
                .nickname("Test User")
                .email("test@example.com")
                .build();
    }
}