package com.antock.api.admin.presentation;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemMetricsController.class)
@DisplayName("SystemMetricsController 테스트")
class SystemMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberApplicationService memberApplicationService;

    @MockBean
    private RateLimitServiceInterface rateLimitService;

    @Test
    @DisplayName("시스템 요약 정보 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getSystemSummary_Success() throws Exception {
        var cacheStats = new com.antock.api.member.application.service.CacheStatistics(
                100L, 80L, 20L, 80.0, 100L, true
        );

        given(memberApplicationService.getCacheStatistics()).willReturn(cacheStats);
        given(rateLimitService.isRedisAvailable()).willReturn(true);

        mockMvc.perform(get("/api/admin/metrics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uptime").exists())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.cache.hitRate").value(80.0))
                .andExpect(jsonPath("$.rateLimit.backend").value("Redis"));
    }

    @Test
    @DisplayName("캐시 성능 메트릭 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getCachePerformanceMetrics_Success() throws Exception {
        var cacheStats = new com.antock.api.member.application.service.CacheStatistics(
                100L, 80L, 20L, 80.0, 100L, true
        );

        given(memberApplicationService.getCacheStatistics()).willReturn(cacheStats);

        mockMvc.perform(get("/api/admin/metrics/cache/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hitRate").value(80.0))
                .andExpect(jsonPath("$.totalRequests").value(100))
                .andExpect(jsonPath("$.cacheHits").value(80))
                .andExpect(jsonPath("$.cacheMisses").value(20));
    }

    @Test
    @DisplayName("속도 제한 성능 메트릭 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getRateLimitPerformanceMetrics_Success() throws Exception {
        given(rateLimitService.isRedisAvailable()).willReturn(false);

        mockMvc.perform(get("/api/admin/metrics/rate-limit/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backend").value("Memory"))
                .andExpect(jsonPath("$.redisAvailable").value(false));
    }

    @Test
    @DisplayName("보안 개요 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getSecurityOverview_Success() throws Exception {
        given(rateLimitService.isRedisAvailable()).willReturn(true);

        mockMvc.perform(get("/api/admin/metrics/security/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rateLimitBackend").value("Redis"))
                .andExpect(jsonPath("$.jwtEnabled").value(true))
                .andExpect(jsonPath("$.securityLevel").value("HIGH"));
    }

    @Test
    @DisplayName("권한 없음 - 일반 사용자")
    @WithMockUser(roles = "USER")
    void getSystemSummary_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/summary"))
                .andExpect(status().isForbidden());
    }
}

