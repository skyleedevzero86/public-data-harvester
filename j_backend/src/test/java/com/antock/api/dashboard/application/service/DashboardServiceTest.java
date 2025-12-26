package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastHistoryRepository;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
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
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService 테스트")
class DashboardServiceTest {

    @Mock
    private CorpMastRepository corpMastRepository;

    @Mock
    private CorpMastHistoryRepository corpMastHistoryRepository;

    @Mock
    private CsvBatchHistoryRepository csvBatchHistoryRepository;

    @Mock
    private Executor asyncExecutor;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        when(corpMastRepository.count()).thenReturn(100L);
        when(corpMastRepository.countValidCorpRegNo()).thenReturn(80L);
        when(corpMastRepository.countValidRegionCd()).thenReturn(75L);
    }

    @Test
    @DisplayName("통계 조회")
    void getStats() {
        DashboardService.DashboardStats stats = dashboardService.getStats();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotal()).isEqualTo(100L);
        assertThat(stats.getValidCorpRegNo()).isEqualTo(80L);
        assertThat(stats.getValidRegionCd()).isEqualTo(75L);
    }

    @Test
    @DisplayName("최근 활동 조회")
    void getRecentActivities() {
        CorpMastHistory history = CorpMastHistory.builder()
                .action("INSERT")
                .bizNo("123-45-67890")
                .timestamp(LocalDateTime.now())
                .build();

        CsvBatchHistory csvHistory = CsvBatchHistory.builder()
                .city("서울특별시")
                .district("강남구")
                .status("SUCCESS")
                .recordCount(100)
                .timestamp(LocalDateTime.now())
                .build();

        when(corpMastHistoryRepository.findTop10ByOrderByTimestampDesc())
                .thenReturn(Arrays.asList(history));
        when(csvBatchHistoryRepository.findTop10ByOrderByTimestampDesc())
                .thenReturn(Arrays.asList(csvHistory));

        List<com.antock.api.dashboard.application.dto.RecentActivityDto> activities =
                dashboardService.getRecentActivities(10);

        assertThat(activities).isNotEmpty();
    }

    @Test
    @DisplayName("빈 통계 조회")
    void getStats_empty() {
        when(corpMastRepository.count()).thenReturn(0L);
        when(corpMastRepository.countValidCorpRegNo()).thenReturn(0L);
        when(corpMastRepository.countValidRegionCd()).thenReturn(0L);

        DashboardService.DashboardStats stats = dashboardService.getStats();

        assertThat(stats.getSuccessRate()).isEqualTo(0.0);
    }
}

