package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.service.converter.HealthCheckResponseConverter;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthCheckQueryService 테스트")
class HealthCheckQueryServiceTest {

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private HealthCheckOrchestrator healthCheckOrchestrator;

    @Mock
    private HealthCheckResponseConverter responseConverter;

    @InjectMocks
    private HealthCheckQueryService healthCheckQueryService;

    private HealthCheck healthCheck;
    private HealthCheckResponse healthCheckResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        healthCheck = HealthCheck.builder()
                .component("database")
                .status(HealthStatus.UP)
                .checkedAt(LocalDateTime.now())
                .build();

        healthCheckResponse = HealthCheckResponse.builder()
                .component("database")
                .status(HealthStatus.UP)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("컴포넌트 헬스 체크 조회")
    void getComponentHealth() {
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);
        when(healthCheckRepository.findByComponentOrderByCheckedAtDesc("database")).thenReturn(healthChecks);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        List<HealthCheckResponse> result = healthCheckQueryService.getComponentHealth("database");

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("컴포넌트 헬스 체크 페이징 조회")
    void getComponentHealth_paged() {
        Page<HealthCheck> healthCheckPage = new PageImpl<>(Arrays.asList(healthCheck));
        when(healthCheckRepository.findByComponentOrderByCheckedAtDesc("database", pageable)).thenReturn(healthCheckPage);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        Page<HealthCheckResponse> result = healthCheckQueryService.getComponentHealth("database", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("헬스 체크 이력 조회")
    void getHealthHistory() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        List<HealthCheck> healthChecks = Arrays.asList(healthCheck);
        when(healthCheckRepository.findByCheckedAtAfterOrderByCheckedAtDesc(fromDate)).thenReturn(healthChecks);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        List<HealthCheckResponse> result = healthCheckQueryService.getHealthHistory(fromDate);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("헬스 체크 이력 페이징 조회")
    void getHealthHistory_paged() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        Page<HealthCheck> healthCheckPage = new PageImpl<>(Arrays.asList(healthCheck));
        when(healthCheckRepository.findRecentChecks(fromDate, pageable)).thenReturn(healthCheckPage);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        Page<HealthCheckResponse> result = healthCheckQueryService.getHealthHistory(fromDate, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("필터링된 헬스 체크 이력 조회 - 컴포넌트와 상태")
    void getHealthHistoryWithFilters_componentAndStatus() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        Page<HealthCheck> healthCheckPage = new PageImpl<>(Arrays.asList(healthCheck));
        when(healthCheckRepository.findByComponentAndStatusAndDateRange("database", HealthStatus.UP, fromDate, toDate, pageable))
                .thenReturn(healthCheckPage);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        Page<HealthCheckResponse> result = healthCheckQueryService.getHealthHistoryWithFilters(
                fromDate, toDate, "database", "UP", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("필터링된 헬스 체크 이력 조회 - 컴포넌트만")
    void getHealthHistoryWithFilters_componentOnly() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        Page<HealthCheck> healthCheckPage = new PageImpl<>(Arrays.asList(healthCheck));
        when(healthCheckRepository.findByComponentAndDateRange("database", fromDate, toDate, pageable))
                .thenReturn(healthCheckPage);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        Page<HealthCheckResponse> result = healthCheckQueryService.getHealthHistoryWithFilters(
                fromDate, toDate, "database", null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("필터링된 헬스 체크 이력 조회 - 상태만")
    void getHealthHistoryWithFilters_statusOnly() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        Page<HealthCheck> healthCheckPage = new PageImpl<>(Arrays.asList(healthCheck));
        when(healthCheckRepository.findByStatusAndDateRange(HealthStatus.UP, fromDate, toDate, pageable))
                .thenReturn(healthCheckPage);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        Page<HealthCheckResponse> result = healthCheckQueryService.getHealthHistoryWithFilters(
                fromDate, toDate, null, "UP", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("필터링된 헬스 체크 이력 조회 - 날짜 범위만")
    void getHealthHistoryWithFilters_dateRangeOnly() {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        Page<HealthCheck> healthCheckPage = new PageImpl<>(Arrays.asList(healthCheck));
        when(healthCheckRepository.findByDateRange(fromDate, toDate, pageable))
                .thenReturn(healthCheckPage);
        when(responseConverter.convertToHealthCheckResponse(healthCheck)).thenReturn(healthCheckResponse);

        Page<HealthCheckResponse> result = healthCheckQueryService.getHealthHistoryWithFilters(
                fromDate, toDate, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("사용 가능한 컴포넌트 목록 조회")
    void getAvailableComponents() {
        List<String> components = Arrays.asList("database", "redis");
        when(healthCheckRepository.findDistinctComponents()).thenReturn(components);

        List<String> result = healthCheckQueryService.getAvailableComponents();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("사용 가능한 컴포넌트 목록 조회 - 빈 결과")
    void getAvailableComponents_empty() {
        when(healthCheckRepository.findDistinctComponents()).thenReturn(Arrays.asList());
        when(healthCheckOrchestrator.getAvailableComponents()).thenReturn(Arrays.asList("database", "redis"));

        List<String> result = healthCheckQueryService.getAvailableComponents();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
    }
}

