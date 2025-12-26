package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthMetricsResponse;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.domain.SystemHealth;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import com.antock.api.health.infrastructure.SystemHealthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthMetricsService 테스트")
class HealthMetricsServiceTest {

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private SystemHealthRepository systemHealthRepository;

    @InjectMocks
    private HealthMetricsService healthMetricsService;

    private HealthCheck healthCheck;
    private SystemHealth systemHealth;

    @BeforeEach
    void setUp() {
        healthCheck = HealthCheck.builder()
                .component("database")
                .status(HealthStatus.UP)
                .responseTime(100L)
                .checkedAt(LocalDateTime.now())
                .build();

        systemHealth = SystemHealth.builder()
                .overallStatus(HealthStatus.UP)
                .totalComponents(5)
                .healthyComponents(5)
                .unhealthyComponents(0)
                .checkedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("시스템 메트릭 계산")
    void calculateSystemMetrics() {
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);
        List<SystemHealth> systemHealths = Arrays.asList(systemHealth);

        when(healthCheckRepository.findByCheckedAtBetweenOrderByCheckedAtDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(healthChecks);
        when(systemHealthRepository.findByCheckedAtBetweenOrderByCheckedAtDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(systemHealths);

        HealthMetricsResponse response = healthMetricsService.calculateSystemMetrics(7);

        assertThat(response).isNotNull();
        assertThat(response.getTotalChecks()).isEqualTo(1L);
    }

    @Test
    @DisplayName("컴포넌트 메트릭 계산")
    void calculateComponentMetrics() {
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);

        when(healthCheckRepository.findByComponentAndCheckedAtBetweenOrderByCheckedAtDesc(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(healthChecks);

        HealthMetricsResponse.ComponentMetrics metrics = healthMetricsService.calculateComponentMetrics("database", 7);

        assertThat(metrics).isNotNull();
        assertThat(metrics.getComponent()).isEqualTo("database");
    }

    @Test
    @DisplayName("실시간 메트릭 계산")
    void calculateRealtimeMetrics() {
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);

        when(healthCheckRepository.findByCheckedAtBetweenOrderByCheckedAtDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(healthChecks);

        HealthMetricsResponse response = healthMetricsService.calculateRealtimeMetrics();

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("실시간 메트릭 계산 - 예외 발생")
    void calculateRealtimeMetrics_exception() {
        when(healthCheckRepository.findByCheckedAtBetweenOrderByCheckedAtDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        HealthMetricsResponse response = healthMetricsService.calculateRealtimeMetrics();

        assertThat(response).isNotNull();
        assertThat(response.getTotalChecks()).isEqualTo(0L);
    }
}

