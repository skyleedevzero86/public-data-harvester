package com.antock.api.health.presentation;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.HealthMetricsResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.application.service.HealthCheckService;
import com.antock.api.health.application.service.HealthCheckQueryService;
import com.antock.api.health.application.service.HealthCheckPaginationService;
import com.antock.api.health.application.service.HealthMetricsService;
import com.antock.api.health.domain.HealthStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthApiController.class)
@DisplayName("HealthApiController 테스트")
class HealthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HealthCheckService healthCheckService;

    @MockBean
    private HealthCheckQueryService healthCheckQueryService;

    @MockBean
    private HealthCheckPaginationService healthCheckPaginationService;

    @MockBean
    private HealthMetricsService healthMetricsService;

    @Test
    @DisplayName("시스템 헬스 상태 조회 성공")
    void getSystemHealth_Success() throws Exception {
        SystemHealthResponse response = SystemHealthResponse.builder()
                .overallStatus(HealthStatus.UP)
                .totalComponents(5)
                .healthyComponents(5)
                .unhealthyComponents(0)
                .healthPercentage(100.0)
                .healthy(true)
                .checkedAt(LocalDateTime.now())
                .build();

        given(healthCheckService.getSystemHealth(any(HealthCheckRequest.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/health/system")
                        .param("ignoreCache", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.overallStatus.code").value("UP"))
                .andExpect(jsonPath("$.data.healthy").value(true));
    }

    @Test
    @DisplayName("수동 헬스 체크 실행 성공")
    @WithMockUser(roles = "ADMIN")
    void performHealthCheck_Success() throws Exception {
        SystemHealthResponse response = SystemHealthResponse.builder()
                .overallStatus(HealthStatus.UP)
                .healthy(true)
                .checkedAt(LocalDateTime.now())
                .build();

        given(healthCheckService.performSystemHealthCheck(anyList(), anyString())).willReturn(response);

        HealthCheckRequest request = HealthCheckRequest.builder()
                .components(Arrays.asList("database", "redis"))
                .build();

        mockMvc.perform(post("/api/v1/health/check")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("컴포넌트 헬스 상태 조회 성공")
    void getComponentHealth_Success() throws Exception {
        HealthCheckResponse healthCheck = HealthCheckResponse.builder()
                .component("database")
                .status(HealthStatus.UP)
                .message("정상")
                .checkedAt(LocalDateTime.now())
                .build();

        Page<HealthCheckResponse> page = new PageImpl<>(Arrays.asList(healthCheck), PageRequest.of(0, 10), 1);
        given(healthCheckQueryService.getComponentHealth(anyString(), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/health/component/database")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].component").value("database"));
    }

    @Test
    @DisplayName("헬스 체크 이력 조회 성공")
    void getHealthHistory_Success() throws Exception {
        HealthCheckResponse healthCheck = HealthCheckResponse.builder()
                .component("database")
                .status(HealthStatus.UP)
                .checkedAt(LocalDateTime.now())
                .build();

        Page<HealthCheckResponse> page = new PageImpl<>(Arrays.asList(healthCheck), PageRequest.of(0, 10), 1);
        given(healthCheckQueryService.getHealthHistory(any(LocalDateTime.class), any())).willReturn(page);

        mockMvc.perform(get("/api/v1/health/history")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("간단한 헬스 상태 조회 성공")
    void getHealthStatus_Success() throws Exception {
        SystemHealthResponse response = SystemHealthResponse.builder()
                .overallStatus(HealthStatus.UP)
                .healthy(true)
                .checkedAt(LocalDateTime.now())
                .build();

        given(healthCheckService.getSystemHealth(any(HealthCheckRequest.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/health/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.healthy").value(true));
    }

    @Test
    @DisplayName("만료된 헬스 체크 데이터 정리 성공")
    @WithMockUser(roles = "ADMIN")
    void cleanupExpiredData_Success() throws Exception {
        given(healthCheckService.cleanupExpiredChecks()).willReturn(10);

        mockMvc.perform(post("/api/v1/health/cleanup")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("헬스 체크 메트릭 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getHealthMetrics_Success() throws Exception {
        HealthMetricsResponse metrics = HealthMetricsResponse.builder()
                .totalChecks(100L)
                .successfulChecks(95L)
                .failedChecks(5L)
                .build();

        given(healthMetricsService.calculateSystemMetrics(anyInt())).willReturn(metrics);

        mockMvc.perform(get("/api/v1/health/metrics")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalChecks").value(100));
    }

    @Test
    @DisplayName("컴포넌트별 메트릭 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getComponentMetrics_Success() throws Exception {
        HealthMetricsResponse.ComponentMetrics metrics = HealthMetricsResponse.ComponentMetrics.builder()
                .component("database")
                .totalChecks(50L)
                .successfulChecks(48L)
                .build();

        given(healthMetricsService.calculateComponentMetrics(anyString(), anyInt())).willReturn(metrics);

        mockMvc.perform(get("/api/v1/health/metrics/component/database")
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.component").value("database"));
    }

    @Test
    @DisplayName("실시간 메트릭 조회 성공")
    void getRealtimeMetrics_Success() throws Exception {
        HealthMetricsResponse metrics = HealthMetricsResponse.builder()
                .totalChecks(10L)
                .successfulChecks(10L)
                .build();

        given(healthMetricsService.calculateRealtimeMetrics()).willReturn(metrics);

        mockMvc.perform(get("/api/v1/health/metrics/realtime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("페이징된 시스템 헬스 상태 조회 성공")
    void getSystemHealthPaged_Success() throws Exception {
        mockMvc.perform(get("/api/v1/health/system/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .param("groupBy", "component"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("페이징된 시스템 헬스 상태 조회 - 잘못된 파라미터")
    void getSystemHealthPaged_InvalidParams() throws Exception {
        mockMvc.perform(get("/api/v1/health/system/paged")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/health/system/paged")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/health/system/paged")
                        .param("groupBy", "invalid"))
                .andExpect(status().isBadRequest());
    }
}

