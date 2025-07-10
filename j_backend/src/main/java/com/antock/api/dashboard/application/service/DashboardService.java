package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastHistoryRepository;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import com.antock.api.dashboard.application.dto.RecentActivityDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final CorpMastRepository corpMastRepository;
    private final CorpMastHistoryRepository corpMastHistoryRepository;
    private final CsvBatchHistoryRepository csvBatchHistoryRepo;

    public DashboardStats getStats() {
        long total = corpMastRepository.count();
        long validCorpRegNo = corpMastRepository.countValidCorpRegNo();
        long validRegionCd = corpMastRepository.countValidRegionCd();

        long autoSuccess = corpMastHistoryRepository.countAutoCollectSuccess();
        long autoTotal = corpMastHistoryRepository.countAutoCollectTotal();
        double successRate = (autoTotal > 0) ? (autoSuccess * 100.0 / autoTotal) : 0.0;

        return new DashboardStats(total, validCorpRegNo, validRegionCd, successRate);
    }

    public List<RecentActivityDto> getRecentActivities(int limit) {
        List<CorpMastHistory> corpHistories = corpMastHistoryRepository.findTop10ByOrderByTimestampDesc();
        List<CsvBatchHistory> csvHistories = csvBatchHistoryRepo.findTop10ByOrderByTimestampDesc();

        List<RecentActivityDto> activities = new ArrayList<>();

        for (CorpMastHistory h : corpHistories) {
            String msg = h.getMessage();
            String type = "info";
            String icon = "info";
            if ("SUCCESS".equals(h.getResult())) { type = "success"; icon = "check_circle"; }
            else if ("FAIL".equals(h.getResult())) { type = "fail"; icon = "error"; }
            activities.add(new RecentActivityDto(
                    msg,
                    timeAgo(h.getTimestamp()),
                    type,
                    icon
            ));
        }

        for (CsvBatchHistory h : csvHistories) {
            String msg = h.getCity() + " " + h.getDistrict() + " 데이터 수집 " + ("SUCCESS".equals(h.getStatus()) ? "완료" : "실패");
            String type = "info";
            String icon = "info";
            if ("SUCCESS".equals(h.getStatus())) { type = "success"; icon = "check_circle"; }
            else if ("FAIL".equals(h.getStatus())) { type = "fail"; icon = "error"; }
            else if ("SKIPPED".equals(h.getStatus())) { type = "info"; icon = "remove_circle"; }
            activities.add(new RecentActivityDto(
                    msg,
                    timeAgo(h.getTimestamp()),
                    type,
                    icon
            ));
        }

        return activities.stream()
                .sorted(Comparator.comparing(RecentActivityDto::getTimeAgo))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private String timeAgo(LocalDateTime time) {
        Duration d = Duration.between(time, LocalDateTime.now());
        if (d.toMinutes() < 1) return "방금 전";
        if (d.toMinutes() < 60) return d.toMinutes() + "분 전";
        if (d.toHours() < 24) return d.toHours() + "시간 전";
        return d.toDays() + "일 전";
    }


    @Getter
    @AllArgsConstructor
    public static class DashboardStats {
        private long total;
        private long validCorpRegNo;
        private long validRegionCd;
        private double successRate;
    }
}