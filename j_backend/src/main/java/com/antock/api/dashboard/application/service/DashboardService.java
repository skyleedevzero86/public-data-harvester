package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastHistoryRepository;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import com.antock.api.dashboard.application.dto.RecentActivityDto;
import com.antock.global.utils.NumberFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final CorpMastRepository corpMastRepository;
    private final CorpMastHistoryRepository corpMastHistoryRepository;
    private final CsvBatchHistoryRepository csvBatchHistoryRepo;
    private final Executor asyncExecutor;

    @Cacheable(value = "dashboardStats", key = "'summary'")
    public DashboardStats getStats() {
        long total = corpMastRepository.count();
        long validCorpRegNo = corpMastRepository.countValidCorpRegNo();
        long validRegionCd = corpMastRepository.countValidRegionCd();
        double successRate = total > 0 ? (double) (validCorpRegNo + validRegionCd) / (2 * total) * 100 : 0.0;

        return new DashboardStats(total, validCorpRegNo, validRegionCd, successRate);
    }

    @Cacheable(value = "recentActivities", key = "#limit")
    public List<RecentActivityDto> getRecentActivities(int limit) {
        List<RecentActivityDto> activities = new ArrayList<>();

        List<CorpMastHistory> corpHistories = corpMastHistoryRepository
                .findTop10ByOrderByTimestampDesc();

        for (CorpMastHistory history : corpHistories) {
            String message = getCorpHistoryMessage(history);
            String icon = getCorpHistoryIcon(history);
            String type = getCorpHistoryType(history);

            activities.add(RecentActivityDto.builder()
                    .message(message)
                    .timeAgo(timeAgo(history.getTimestamp()))
                    .type(type)
                    .icon(icon)
                    .build());
        }

        List<com.antock.api.csv.domain.CsvBatchHistory> csvHistories = csvBatchHistoryRepo
                .findTop10ByOrderByTimestampDesc();

        for (com.antock.api.csv.domain.CsvBatchHistory history : csvHistories) {
            String message = getCsvHistoryMessage(history);
            String icon = getCsvHistoryIcon(history);
            String type = getCsvHistoryType(history);

            activities.add(RecentActivityDto.builder()
                    .message(message)
                    .timeAgo(timeAgo(history.getTimestamp()))
                    .type(type)
                    .icon(icon)
                    .build());
        }

        activities.addAll(getSystemStatusActivities());

        return activities.stream()
                .sorted((a, b) -> b.getTimeAgo().compareTo(a.getTimeAgo()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    private String getCorpHistoryMessage(CorpMastHistory history) {
        String action = history.getAction();
        String bizNo = history.getBizNo();
        String message = history.getMessage();

        if (message != null && !message.trim().isEmpty()) {
            return message;
        }

        switch (action) {
            case "INSERT":
                return "새로운 법인 정보가 등록되었습니다: " + bizNo;
            case "UPDATE":
                return "법인 정보가 수정되었습니다: " + bizNo;
            case "DELETE":
                return "법인 정보가 삭제되었습니다: " + bizNo;
            default:
                return "법인 정보 " + action + ": " + bizNo;
        }
    }

    private String getCorpHistoryIcon(CorpMastHistory history) {
        String action = history.getAction();

        switch (action) {
            case "INSERT":
                return "➕";
            case "UPDATE":
                return "✏️";
            case "DELETE":
                return "🗑️";
            default:
                return "🏢";
        }
    }

    private String getCorpHistoryType(CorpMastHistory history) {
        String action = history.getAction();

        switch (action) {
            case "INSERT":
                return "success";
            case "UPDATE":
                return "info";
            case "DELETE":
                return "error";
            default:
                return "warning";
        }
    }

    private String getCsvHistoryMessage(com.antock.api.csv.domain.CsvBatchHistory history) {
        String city = history.getCity();
        String district = history.getDistrict();
        String status = history.getStatus();
        int recordCount = history.getRecordCount();

        switch (status) {
            case "SUCCESS":
                return String.format("CSV 배치 처리 완료: %s %s (%d건)", city, district, recordCount);
            case "FAILED":
                return String.format("CSV 배치 처리 실패: %s %s", city, district);
            case "PROCESSING":
                return String.format("CSV 배치 처리 중: %s %s", city, district);
            default:
                return String.format("CSV 배치 %s: %s %s", status, city, district);
        }
    }

    private String getCsvHistoryIcon(com.antock.api.csv.domain.CsvBatchHistory history) {
        String status = history.getStatus();

        switch (status) {
            case "SUCCESS":
                return "✅";
            case "FAILED":
                return "❌";
            case "PROCESSING":
                return "⏳";
            default:
                return "📊";
        }
    }

    private String getCsvHistoryType(com.antock.api.csv.domain.CsvBatchHistory history) {
        String status = history.getStatus();

        switch (status) {
            case "SUCCESS":
                return "success";
            case "FAILED":
                return "error";
            case "PROCESSING":
                return "warning";
            default:
                return "info";
        }
    }

    private List<RecentActivityDto> getSystemStatusActivities() {
        List<RecentActivityDto> systemActivities = new ArrayList<>();

        systemActivities.add(RecentActivityDto.builder()
                .message("시스템이 정상적으로 실행 중입니다")
                .timeAgo("방금 전")
                .type("info")
                .icon("🟢")
                .build());

        try {
            long totalCount = corpMastRepository.count();
            systemActivities.add(RecentActivityDto.builder()
                    .message("데이터베이스 연결 확인됨")
                    .timeAgo("1분 전")
                    .type("success")
                    .icon("🟢")
                    .build());
        } catch (Exception e) {
            systemActivities.add(RecentActivityDto.builder()
                    .message("데이터베이스 연결 오류")
                    .timeAgo("방금 전")
                    .type("error")
                    .icon("🔴")
                    .build());
        }

        systemActivities.add(RecentActivityDto.builder()
                .message("새로운 데이터 수집 대기 중")
                .timeAgo("5분 전")
                .type("warning")
                .icon("🟡")
                .build());

        return systemActivities;
    }

    private String timeAgo(LocalDateTime time) {
        if (time == null)
            return "알 수 없음";

        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 60)
            return minutes + "분 전";

        long hours = ChronoUnit.HOURS.between(time, LocalDateTime.now());
        if (hours < 24)
            return hours + "시간 전";

        long days = ChronoUnit.DAYS.between(time, LocalDateTime.now());
        return days + "일 전";
    }

    @Scheduled(fixedDelay = 300000)
    @Cacheable(value = "dashboardStats", key = "'refresh'")
    public void refreshStats() {
        log.info("대시보드 통계 정보 새로고침");
    }

    @Getter
    @AllArgsConstructor
    public static class DashboardStats {
        private long total;
        private long validCorpRegNo;
        private long validRegionCd;
        private double successRate;

        public String getFormattedTotal() {
            return NumberFormatUtil.formatLong(total);
        }

        public String getFormattedValidCorpRegNo() {
            return NumberFormatUtil.formatLong(validCorpRegNo);
        }

        public String getFormattedValidRegionCd() {
            return NumberFormatUtil.formatLong(validRegionCd);
        }

        public String getFormattedSuccessRate() {
            return String.format("%.1f%%", successRate);
        }

    }

}