package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import com.antock.api.health.infrastructure.SystemHealthRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckService 테스트")
class HealthCheckServiceTest {

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private SystemHealthRepository systemHealthRepository;

    @Mock
    private HealthCheckOrchestrator healthCheckOrchestrator;

    @Mock
    private Executor asyncExecutor;

    private HealthCheckService healthCheckService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        healthCheckService = new HealthCheckService(
                healthCheckRepository,
                systemHealthRepository,
                healthCheckOrchestrator,
                objectMapper,
                asyncExecutor
        );
    }

    @Test
    @DisplayName("시스템 헬스 체크 요청 처리")
    void getSystemHealth() {
        when(healthCheckOrchestrator.getAvailableComponents())
                .thenReturn(Arrays.asList("database", "redis"));
        when(systemHealthRepository.findLatestValidSystemHealth(any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        HealthCheckRequest request = HealthCheckRequest.builder()
                .components(Arrays.asList("database"))
                .checkType("test")
                .ignoreCache(false)
                .build();

        SystemHealthResponse response = healthCheckService.getSystemHealth(request);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("컴포넌트 헬스 체크 수행")
    void performComponentHealthCheck() {
        HealthCheck healthCheck = HealthCheck.builder()
                .component("database")
                .status(HealthStatus.UP)
                .message("정상")
                .responseTime(10L)
                .checkType("test")
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(300))
                .build();

        when(healthCheckOrchestrator.performComponentHealthCheck("database", "test"))
                .thenReturn(healthCheck);

        HealthCheck result = healthCheckService.performComponentHealthCheck("database", "test");

        assertThat(result.getComponent()).isEqualTo("database");
        assertThat(result.getStatus()).isEqualTo(HealthStatus.UP);
    }

    @Test
    @DisplayName("헬스 체크 중 예외 발생 시 BusinessException으로 변환")
    void performComponentHealthCheck_exception() {
        when(healthCheckOrchestrator.performComponentHealthCheck("database", "test"))
                .thenThrow(new RuntimeException("예상치 못한 오류"));

        assertThatThrownBy(() -> healthCheckService.performComponentHealthCheck("database", "test"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException be = (BusinessException) exception;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
                });
    }

    @Test
    @DisplayName("사용 가능한 컴포넌트 목록 반환")
    void getAvailableComponents() {
        List<String> components = Arrays.asList("database", "redis", "cache");
        when(healthCheckOrchestrator.getAvailableComponents()).thenReturn(components);
        when(healthCheckRepository.findDistinctComponents()).thenReturn(components);

        List<String> result = healthCheckService.getAvailableComponents();

        assertThat(result).containsExactlyElementsOf(components);
    }
}

