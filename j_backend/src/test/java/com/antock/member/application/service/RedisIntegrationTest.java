package com.antock.member.application.service;

import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.MemberCacheService;
import com.antock.api.member.application.service.RedisRateLimitService;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private MemberCacheService memberCacheService;

    @Autowired
    private RedisRateLimitService rateLimitService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("custom.redis.enabled", () -> "true");
    }

    @Test
    @DisplayName("실제 Redis를 사용한 캐시 기능 검증")
    void realRedisCache_IntegrationTest() {
        MemberResponse memberResponse = createTestMemberResponse();
        Long memberId = memberResponse.getId();

        memberCacheService.cacheMemberResponse(memberResponse);

        MemberResponse cachedResult = memberCacheService.getMemberFromCache(memberId);
        assertThat(cachedResult).isNotNull();
        assertThat(cachedResult.getId()).isEqualTo(memberId);
        assertThat(cachedResult.getUsername()).isEqualTo("testuser");

        memberCacheService.evictMemberCache(memberId);

        MemberResponse evictedResult = memberCacheService.getMemberFromCache(memberId);
        assertThat(evictedResult).isNull();
    }

    @Test
    @DisplayName("실제 Redis를 사용한 속도 제한 기능 검증")
    void realRedisRateLimit_IntegrationTest() {
        String identifier = "integration-test-user";
        String action = "test-action";

        assertThatCode(() -> {
            for (int i = 0; i < 5; i++) {
                rateLimitService.checkRateLimit(identifier, action);
            }
        }).doesNotThrowAnyException();

        int currentCount = rateLimitService.getCurrentCount(identifier, action);
        assertThat(currentCount).isEqualTo(5);

        rateLimitService.resetLimit(identifier, action);
        int resetCount = rateLimitService.getCurrentCount(identifier, action);
        assertThat(resetCount).isEqualTo(0);
    }

    @Test
    @DisplayName("캐시 통계 기능 검증")
    void cacheStatistics_IntegrationTest() {
        MemberResponse memberResponse = createTestMemberResponse();
        Long memberId = memberResponse.getId();

 후 여러 번 조회
        memberCacheService.cacheMemberResponse(memberResponse);

        memberCacheService.getMemberFromCache(memberId);
        memberCacheService.getMemberFromCache(memberId);
        memberCacheService.getMemberFromCache(999L);

        MemberCacheService.CacheStatistics stats = memberCacheService.getCacheStatistics();
        assertThat(stats.getCacheHits()).isEqualTo(2);
        assertThat(stats.getCacheMisses()).isEqualTo(1);
        assertThat(stats.getTotalRequests()).isEqualTo(3);
        assertThat(stats.getHitRate()).isCloseTo(66.67, within(0.1));
        assertThat(stats.isCacheAvailable()).isTrue();
    }

    @Test
    @DisplayName("Redis 연결 상태 확인")
    void redisConnectionHealth_Test() {
        boolean isRedisAvailable = rateLimitService.isRedisAvailable();

        assertThat(isRedisAvailable).isTrue();
    }

    private MemberResponse createTestMemberResponse() {
        return MemberResponse.builder()
                .id(100L)
                .username("testuser")
                .nickname("Test User")
                .email("test@example.com")
                .status(MemberStatus.APPROVED)
                .role(Role.USER)
                .build();
    }
}