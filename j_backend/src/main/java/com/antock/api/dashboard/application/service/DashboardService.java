package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastHistoryRepository;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import com.antock.api.dashboard.application.dto.RecentActivityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CorpMastRepository corpMastRepository;
    private final CorpMastHistoryRepository corpMastHistoryRepository;
    private final CsvBatchHistoryRepository csvBatchHistoryRepo;
    private final Executor asyncExecutor;

    @Cacheable(value = "dashboardStats", key = "'summary'")
    public DashboardStats getStats() {
        CompletableFuture<Long> totalFuture = CompletableFuture.supplyAsync(
                () -> corpMastRepository.count(), asyncExecutor);

        CompletableFuture<Long> validCorpRegNoFuture = CompletableFuture.supplyAsync(
                () -> corpMastRepository.countValidCorpRegNo(), asyncExecutor);

        CompletableFuture<Long> validRegionCdFuture = CompletableFuture.supplyAsync(
                () -> corpMastRepository.countValidRegionCd(), asyncExecutor);

        CompletableFuture.allOf(totalFuture, validCorpRegNoFuture, validRegionCdFuture).join();

        long total = totalFuture.getNow(0L);
        long validCorpRegNo = validCorpRegNoFuture.getNow(0L);
        long validRegionCd = validRegionCdFuture.getNow(0L);

        double successRate = total > 0 ? (double) (validCorpRegNo + validRegionCd) / (total * 2) * 100 : 0;

        return new DashboardStats(total, validCorpRegNo, validRegionCd, successRate);
    }

    @Cacheable(value = "recentActivities", key = "#limit")
    public List<RecentActivityDto> getRecentActivities(int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 100));

        List<RecentActivityDto> activities = new ArrayList<>();

        List<CorpMastHistory> corpHistories = corpMastHistoryRepository
                .findTop10ByOrderByTimestampDesc();

        for (CorpMastHistory history : corpHistories) {
            activities.add(RecentActivityDto.builder()
                    .message(String.format("법인정보 %s: %s", history.getAction(), history.getBizNo()))
                    .timeAgo(timeAgo(history.getTimestamp()))
                    .type("corp")
                    .icon("🏢")
                    .build());
        }

        List<com.antock.api.csv.domain.CsvBatchHistory> csvHistories = csvBatchHistoryRepo
                .findTop10ByOrderByTimestampDesc();

        for (com.antock.api.csv.domain.CsvBatchHistory history : csvHistories) {
            activities.add(RecentActivityDto.builder()
                    .message(String.format("CSV 배치 처리: %s %s", history.getCity(), history.getDistrict()))
                    .timeAgo(timeAgo(history.getTimestamp()))
                    .type("csv")
                    .icon("📊")
                    .build());
        }

        return activities.stream()
                .sorted((a, b) -> b.getTimeAgo().compareTo(a.getTimeAgo()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    private String timeAgo(LocalDateTime time) {
        if (time == null) return "알 수 없음";

        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 60) return minutes + "분 전";

        long hours = ChronoUnit.HOURS.between(time, LocalDateTime.now());
        if (hours < 24) return hours + "시간 전";

        long days = ChronoUnit.DAYS.between(time, LocalDateTime.now());
        return days + "일 전";
    }

    @Getter
    @AllArgsConstructor
    public static class DashboardStats {
        private long total;
        private long validCorpRegNo;
        private long validRegionCd;
        private double successRate;
    }

    @Scheduled(fixedDelay = 300000)
    @Cacheable(value = "dashboardStats", key = "'refresh'")
    public void refreshStats() {
        log.info("대시보드 통계 정보 새로고침");
    }
}