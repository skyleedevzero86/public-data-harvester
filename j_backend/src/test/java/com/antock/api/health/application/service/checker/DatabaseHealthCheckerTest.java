package com.antock.api.health.application.service.checker;

import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseHealthChecker 테스트")
class DatabaseHealthCheckerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private DatabaseHealthChecker databaseHealthChecker;

    @BeforeEach
    void setUp() {
        databaseHealthChecker = new DatabaseHealthChecker(dataSource);
    }

    @Test
    @DisplayName("컴포넌트 이름은 database여야 함")
    void getComponentName() {
        assertThat(databaseHealthChecker.getComponentName()).isEqualTo("database");
    }

    @Test
    @DisplayName("데이터베이스 연결이 정상이면 UP 상태 반환")
    void check_success() {
        ReflectionTestUtils.setField(databaseHealthChecker, "dataSource", dataSource);
        
        JdbcTemplate template = new JdbcTemplate(dataSource);
        when(template.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(1);

        HealthCheckResult result = databaseHealthChecker.check();

        assertThat(result.getStatus()).isEqualTo(HealthStatus.UP);
        assertThat(result.getMessage()).contains("데이터베이스 연결 정상");
        assertThat(result.getResponseTime()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("데이터베이스 연결 실패 시 BusinessException 발생")
    void check_failure() {
        ReflectionTestUtils.setField(databaseHealthChecker, "dataSource", dataSource);
        
        JdbcTemplate template = new JdbcTemplate(dataSource);
        when(template.queryForObject(eq("SELECT 1"), eq(Integer.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        assertThatThrownBy(() -> databaseHealthChecker.check())
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException be = (BusinessException) exception;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
                });
    }
}

