package com.antock.api.health.application.service.converter;

import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.domain.SystemHealth;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckResponseConverter 테스트")
class HealthCheckResponseConverterTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HealthCheckResponseConverter converter;

    private HealthCheck healthCheck;
    private SystemHealth systemHealth;

    @BeforeEach
    void setUp() {
        converter = new HealthCheckResponseConverter(new ObjectMapper());

        healthCheck = HealthCheck.builder()
                .component("database")
                .status(HealthStatus.UP)
                .message("OK")
                .responseTime(100L)
                .checkType("DATABASE")
                .details("{\"key\":\"value\"}")
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        systemHealth = SystemHealth.builder()
                .overallStatus(HealthStatus.UP)
                .totalComponents(5)
                .healthyComponents(5)
                .unhealthyComponents(0)
                .unknownComponents(0)
                .details("{\"key\":\"value\"}")
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
    }

    @Test
    @DisplayName("HealthCheck를 HealthCheckResponse로 변환")
    void convertToHealthCheckResponse() {
        HealthCheckResponse response = converter.convertToHealthCheckResponse(healthCheck);

        assertThat(response).isNotNull();
        assertThat(response.getComponent()).isEqualTo("database");
        assertThat(response.getStatus()).isEqualTo(HealthStatus.UP);
    }

    @Test
    @DisplayName("SystemHealth를 SystemHealthResponse로 변환")
    void convertToSystemHealthResponse() {
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);

        SystemHealthResponse response = converter.convertToSystemHealthResponse(systemHealth, healthChecks);

        assertThat(response).isNotNull();
        assertThat(response.getOverallStatus()).isEqualTo(HealthStatus.UP);
        assertThat(response.getTotalComponents()).isEqualTo(5);
    }

    @Test
    @DisplayName("Details를 JSON으로 변환")
    void convertDetailsToJson() {
        Map<String, Object> details = new HashMap<>();
        details.put("key", "value");

        String json = converter.convertDetailsToJson(details);

        assertThat(json).isNotNull();
        assertThat(json).contains("key");
    }

    @Test
    @DisplayName("JSON을 Details로 파싱")
    void parseDetailsFromJson() {
        String json = "{\"key\":\"value\"}";

        Map<String, Object> details = converter.parseDetailsFromJson(json);

        assertThat(details).isNotNull();
        assertThat(details).containsKey("key");
    }

    @Test
    @DisplayName("JSON을 Details로 파싱 - null")
    void parseDetailsFromJson_null() {
        Map<String, Object> details = converter.parseDetailsFromJson(null);

        assertThat(details).isNotNull();
        assertThat(details).isEmpty();
    }

    @Test
    @DisplayName("JSON을 Details로 파싱 - 빈 문자열")
    void parseDetailsFromJson_empty() {
        Map<String, Object> details = converter.parseDetailsFromJson("");

        assertThat(details).isNotNull();
        assertThat(details).isEmpty();
    }
}

