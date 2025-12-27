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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CacheMonitoringController.class)
@DisplayName("CacheMonitoringController 테스트")
class CacheMonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberApplicationService memberApplicationService;

    @MockBean
    private RateLimitServiceInterface rateLimitService;

    @Test
    @DisplayName("캐시 통계 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getCacheStatistics_Success() throws Exception {
        var stats = new com.antock.api.member.application.service.MemberCacheService.CacheStatistics(
                100L, 80L, 20L, 80.0, 100L, true
        );

        given(memberApplicationService.getCacheStatistics()).willReturn(stats);

        mockMvc.perform(get("/api/admin/cache/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hitRate").value(80.0))
                .andExpect(jsonPath("$.totalRequests").value(100));
    }

    @Test
    @DisplayName("전체 회원 캐시 무효화 성공")
    @WithMockUser(roles = "ADMIN")
    void evictAllMemberCache_Success() throws Exception {
        doNothing().when(memberApplicationService).evictAllMemberCache();

        mockMvc.perform(delete("/api/admin/cache/members")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("특정 회원 캐시 무효화 성공")
    @WithMockUser(roles = "ADMIN")
    void evictMemberCache_Success() throws Exception {
        doNothing().when(memberApplicationService).evictMemberCache(anyLong());

        mockMvc.perform(delete("/api/admin/cache/members/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.memberId").value(1));
    }

    @Test
    @DisplayName("속도 제한 상태 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getRateLimitStatus_Success() throws Exception {
        given(rateLimitService.isRedisAvailable()).willReturn(true);

        mockMvc.perform(get("/api/admin/cache/rate-limit/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redisAvailable").value(true))
                .andExpect(jsonPath("$.backend").value("Redis"));
    }

    @Test
    @DisplayName("속도 제한 재설정 성공")
    @WithMockUser(roles = "ADMIN")
    void resetRateLimit_Success() throws Exception {
        given(rateLimitService.getCurrentCount(anyString(), anyString())).willReturn(10, 0);
        doNothing().when(rateLimitService).resetLimit(anyString(), anyString());

        mockMvc.perform(delete("/api/admin/cache/rate-limit/testuser/login")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("속도 제한 카운트 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getCurrentRateLimit_Success() throws Exception {
        given(rateLimitService.getCurrentCount(anyString(), anyString())).willReturn(5);
        given(rateLimitService.isRedisAvailable()).willReturn(true);

        mockMvc.perform(get("/api/admin/cache/rate-limit/testuser/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentCount").value(5))
                .andExpect(jsonPath("$.backend").value("Redis"));
    }

    @Test
    @DisplayName("식별자 차단 성공")
    @WithMockUser(roles = "ADMIN")
    void blockIdentifier_Success() throws Exception {
        doNothing().when(rateLimitService).blockIdentifier(anyString(), anyString(), anyLong());

        mockMvc.perform(post("/api/admin/cache/rate-limit/security/block/testuser")
                        .with(csrf())
                        .param("reason", "테스트 차단")
                        .param("blockDurationMinutes", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("식별자 차단 해제 성공")
    @WithMockUser(roles = "ADMIN")
    void unblockIdentifier_Success() throws Exception {
        doNothing().when(rateLimitService).unblockIdentifier(anyString());

        mockMvc.perform(post("/api/admin/cache/rate-limit/security/unblock/testuser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("화이트리스트 추가 성공")
    @WithMockUser(roles = "ADMIN")
    void addToWhitelist_Success() throws Exception {
        doNothing().when(rateLimitService).addToWhitelist(anyString());

        mockMvc.perform(post("/api/admin/cache/rate-limit/security/whitelist/testuser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("화이트리스트 제거 성공")
    @WithMockUser(roles = "ADMIN")
    void removeFromWhitelist_Success() throws Exception {
        doNothing().when(rateLimitService).removeFromWhitelist(anyString());

        mockMvc.perform(delete("/api/admin/cache/rate-limit/security/whitelist/testuser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

