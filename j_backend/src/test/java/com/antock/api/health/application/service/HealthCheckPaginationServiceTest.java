package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.PagedSystemHealthResponse;
import com.antock.api.health.application.service.converter.HealthCheckResponseConverter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckPaginationService 테스트")
class HealthCheckPaginationServiceTest {

    @Mock
    private SystemHealthRepository systemHealthRepository;

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private HealthCheckResponseConverter responseConverter;

    @InjectMocks
    private HealthCheckPaginationService healthCheckPaginationService;

    private SystemHealth systemHealth;
    private HealthCheck healthCheck;

    @BeforeEach
    void setUp() {
        systemHealth = SystemHealth.builder()
                .overallStatus(HealthStatus.UP)
                .totalComponents(5)
                .healthyComponents(5)
                .unhealthyComponents(0)
                .unknownComponents(0)
                .details("{}")
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        healthCheck = HealthCheck.builder()
                .component("database")
                .status(HealthStatus.UP)
                .checkedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("페이징된 시스템 헬스 체크 조회 성공")
    void getSystemHealthPaged_success() {
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);
        when(systemHealthRepository.findTopByOrderByCheckedAtDesc()).thenReturn(Optional.of(systemHealth));
        when(healthCheckRepository.findAllOrderByCheckedAtDesc()).thenReturn(healthChecks);
        when(responseConverter.parseDetailsFromJson(anyString())).thenReturn(new HashMap<>());
        when(responseConverter.convertToHealthCheckResponse(any(HealthCheck.class))).thenReturn(
                com.antock.api.health.application.dto.HealthCheckResponse.builder()
                        .component("database")
                        .status(HealthStatus.UP)
                        .build());

        PagedSystemHealthResponse response = healthCheckPaginationService.getSystemHealthPaged(0, 10, "component");

        assertThat(response).isNotNull();
        assertThat(response.getOverallStatus()).isEqualTo(HealthStatus.UP);
        assertThat(response.getTotalComponents()).isEqualTo(5);
    }

    @Test
    @DisplayName("페이징된 시스템 헬스 체크 조회 - 빈 결과")
    void getSystemHealthPaged_empty() {
        when(systemHealthRepository.findTopByOrderByCheckedAtDesc()).thenReturn(Optional.empty());

        PagedSystemHealthResponse response = healthCheckPaginationService.getSystemHealthPaged(0, 10, "component");

        assertThat(response).isNotNull();
        assertThat(response.getOverallStatus()).isEqualTo(HealthStatus.UNKNOWN);
        assertThat(response.getTotalComponents()).isEqualTo(0);
    }

    @Test
    @DisplayName("페이징된 시스템 헬스 체크 조회 - 상태별 그룹핑")
    void getSystemHealthPaged_groupByStatus() {
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);
        when(systemHealthRepository.findTopByOrderByCheckedAtDesc()).thenReturn(Optional.of(systemHealth));
        when(healthCheckRepository.findAllOrderByCheckedAtDesc()).thenReturn(healthChecks);
        when(responseConverter.parseDetailsFromJson(anyString())).thenReturn(new HashMap<>());
        when(responseConverter.convertToHealthCheckResponse(any(HealthCheck.class))).thenReturn(
                com.antock.api.health.application.dto.HealthCheckResponse.builder()
                        .component("database")
                        .status(HealthStatus.UP)
                        .build());

        PagedSystemHealthResponse response = healthCheckPaginationService.getSystemHealthPaged(0, 10, "status");

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("페이징된 시스템 헬스 체크 조회 - 대용량 데이터")
    void getSystemHealthPaged_largeData() {
        List<HealthCheck> largeList = new java.util.ArrayList<>();
        for (int i = 0; i < 15000; i++) {
            largeList.add(healthCheck);
        }

        when(systemHealthRepository.findTopByOrderByCheckedAtDesc()).thenReturn(Optional.of(systemHealth));
        when(healthCheckRepository.findAllOrderByCheckedAtDesc()).thenReturn(largeList);
        when(responseConverter.parseDetailsFromJson(anyString())).thenReturn(new HashMap<>());
        when(responseConverter.convertToHealthCheckResponse(any(HealthCheck.class))).thenReturn(
                com.antock.api.health.application.dto.HealthCheckResponse.builder()
                        .component("database")
                        .status(HealthStatus.UP)
                        .build());

        PagedSystemHealthResponse response = healthCheckPaginationService.getSystemHealthPaged(0, 10, "component");

        assertThat(response).isNotNull();
    }
}

