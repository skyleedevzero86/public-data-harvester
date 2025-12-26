package com.antock.api.health.application.service;

import com.antock.api.health.application.service.checker.ComponentHealthChecker;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckOrchestrator 테스트")
class HealthCheckOrchestratorTest {

    @Mock
    private ComponentHealthChecker databaseChecker;

    @Mock
    private ComponentHealthChecker redisChecker;

    private HealthCheckOrchestrator orchestrator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        List<ComponentHealthChecker> checkers = Arrays.asList(databaseChecker, redisChecker);
        orchestrator = new HealthCheckOrchestrator(checkers, objectMapper);
    }

    @Test
    @DisplayName("사용 가능한 컴포넌트 목록 반환")
    void getAvailableComponents() {
        when(databaseChecker.getComponentName()).thenReturn("database");
        when(redisChecker.getComponentName()).thenReturn("redis");

        List<String> components = orchestrator.getAvailableComponents();

        assertThat(components).containsExactly("database", "redis");
    }

    @Test
    @DisplayName("정상적인 헬스 체크 수행")
    void performComponentHealthCheck_success() {
        when(databaseChecker.getComponentName()).thenReturn("database");
        Map<String, Object> details = new HashMap<>();
        details.put("responseTime", 10L);
        HealthCheckResult result = new HealthCheckResult(HealthStatus.UP, "정상", details, 10L);
        
        when(databaseChecker.check()).thenReturn(result);

        HealthCheck healthCheck = orchestrator.performComponentHealthCheck("database", "test");

        assertThat(healthCheck.getComponent()).isEqualTo("database");
        assertThat(healthCheck.getStatus()).isEqualTo(HealthStatus.UP);
        assertThat(healthCheck.getMessage()).isEqualTo("정상");
        assertThat(healthCheck.getCheckType()).isEqualTo("test");
    }

    @Test
    @DisplayName("알 수 없는 컴포넌트는 BusinessException 발생")
    void performComponentHealthCheck_unknownComponent() {
        when(databaseChecker.getComponentName()).thenReturn("database");
        when(redisChecker.getComponentName()).thenReturn("redis");

        assertThatThrownBy(() -> orchestrator.performComponentHealthCheck("unknown", "test"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException be = (BusinessException) exception;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
                });
    }

    @Test
    @DisplayName("헬스 체크 중 BusinessException 발생 시 DOWN 상태로 처리")
    void performComponentHealthCheck_businessException() {
        when(databaseChecker.getComponentName()).thenReturn("database");
        when(databaseChecker.check())
                .thenThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "데이터베이스 오류"));

        HealthCheck healthCheck = orchestrator.performComponentHealthCheck("database", "test");

        assertThat(healthCheck.getStatus()).isEqualTo(HealthStatus.DOWN);
        assertThat(healthCheck.getMessage()).contains("비즈니스 오류");
    }
}

