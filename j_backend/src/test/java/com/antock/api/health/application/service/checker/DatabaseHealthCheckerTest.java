package com.antock.api.health.application.service.checker;

import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseHealthChecker 테스트")
class DatabaseHealthCheckerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private DatabaseHealthChecker databaseHealthChecker;

    @BeforeEach
    void setUp() {
        databaseHealthChecker = new DatabaseHealthChecker(entityManager);
    }

    @Test
    @DisplayName("컴포넌트 이름은 database여야 함")
    void getComponentName() {
        assertThat(databaseHealthChecker.getComponentName()).isEqualTo("database");
    }

    @Test
    @DisplayName("데이터베이스 연결이 정상이면 UP 상태 반환")
    void check_success() {
        when(entityManager.createNativeQuery("SELECT 1")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1);

        HealthCheckResult result = databaseHealthChecker.check();

        assertThat(result.getStatus()).isEqualTo(HealthStatus.UP);
        assertThat(result.getMessage()).contains("데이터베이스 연결 정상");
    }

    @Test
    @DisplayName("데이터베이스 연결 실패 시 DOWN 상태 반환")
    void check_failure() {
        when(entityManager.createNativeQuery("SELECT 1")).thenThrow(new RuntimeException("Connection failed"));

        HealthCheckResult result = databaseHealthChecker.check();

        assertThat(result.getStatus()).isEqualTo(HealthStatus.DOWN);
        assertThat(result.getMessage()).contains("데이터베이스 연결 실패");
    }
}
