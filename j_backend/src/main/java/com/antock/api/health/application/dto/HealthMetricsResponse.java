package com.antock.api.health.application.dto;

import com.antock.api.health.domain.HealthStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "헬스 체크 메트릭 응답 DTO")
public class HealthMetricsResponse {

    @Schema(description = "전체 시스템 가용성 (백분율)", example = "99.5")
    private Double overallAvailability;

    @Schema(description = "평균 응답 시간 (밀리초)", example = "150.5")
    private Double averageResponseTime;

    @Schema(description = "최대 응답 시간 (밀리초)", example = "500.0")
    private Double maxResponseTime;

    @Schema(description = "최소 응답 시간 (밀리초)", example = "50.0")
    private Double minResponseTime;

    @Schema(description = "총 체크 횟수", example = "1000")
    private Long totalChecks;

    @Schema(description = "성공한 체크 횟수", example = "995")
    private Long successfulChecks;

    @Schema(description = "실패한 체크 횟수", example = "5")
    private Long failedChecks;

    @Schema(description = "성공률 (백분율)", example = "99.5")
    private Double successRate;

    @Schema(description = "컴포넌트별 메트릭")
    private List<ComponentMetrics> componentMetrics;

    @Schema(description = "시간대별 통계")
    private List<TimeBasedMetrics> timeBasedMetrics;

    @Schema(description = "상태별 분포")
    private Map<HealthStatus, Long> statusDistribution;

    @Schema(description = "최근 24시간 트렌드")
    private List<HourlyMetrics> hourlyTrends;

    @Schema(description = "평균 가동 시간 (시간)", example = "23.8")
    private Double averageUptime;

    @Schema(description = "평균 다운 시간 (시간)", example = "0.2")
    private Double averageDowntime;

    @Schema(description = "MTBF (평균 고장 간격, 시간)", example = "120.5")
    private Double mtbf;

    @Schema(description = "MTTR (평균 복구 시간, 시간)", example = "0.5")
    private Double mttr;

    @Schema(description = "메트릭 계산 시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime calculatedAt;

    @Schema(description = "분석 기간 시작")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime periodStart;

    @Schema(description = "분석 기간 종료")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime periodEnd;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "컴포넌트별 메트릭")
    public static class ComponentMetrics {
        @Schema(description = "컴포넌트명", example = "database")
        private String component;

        @Schema(description = "가용성 (백분율)", example = "99.8")
        private Double availability;

        @Schema(description = "평균 응답 시간 (밀리초)", example = "120.5")
        private Double averageResponseTime;

        @Schema(description = "최대 응답 시간 (밀리초)", example = "300.0")
        private Double maxResponseTime;

        @Schema(description = "최소 응답 시간 (밀리초)", example = "50.0")
        private Double minResponseTime;

        @Schema(description = "총 체크 횟수", example = "1000")
        private Long totalChecks;

        @Schema(description = "성공한 체크 횟수", example = "998")
        private Long successfulChecks;

        @Schema(description = "실패한 체크 횟수", example = "2")
        private Long failedChecks;

        @Schema(description = "성공률 (백분율)", example = "99.8")
        private Double successRate;

        @Schema(description = "마지막 체크 시간")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastCheckTime;

        @Schema(description = "마지막 상태", example = "UP")
        private HealthStatus lastStatus;

        @Schema(description = "연속 실패 횟수", example = "0")
        private Integer consecutiveFailures;

        @Schema(description = "연속 성공 횟수", example = "150")
        private Integer consecutiveSuccesses;

        public Date getLastCheckTimeAsDate() {
            return lastCheckTime != null ? Date.from(lastCheckTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "시간대별 메트릭")
    public static class TimeBasedMetrics {
        @Schema(description = "시간대", example = "2024-01-15T10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timeSlot;

        @Schema(description = "해당 시간대 체크 횟수", example = "60")
        private Long checkCount;

        @Schema(description = "해당 시간대 성공 횟수", example = "59")
        private Long successCount;

        @Schema(description = "해당 시간대 실패 횟수", example = "1")
        private Long failureCount;

        @Schema(description = "해당 시간대 평균 응답 시간", example = "145.5")
        private Double averageResponseTime;

        @Schema(description = "해당 시간대 가용성", example = "98.3")
        private Double availability;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "시간별 메트릭")
    public static class HourlyMetrics {
        @Schema(description = "시간", example = "14")
        private Integer hour;

        @Schema(description = "해당 시간 체크 횟수", example = "60")
        private Long checkCount;

        @Schema(description = "해당 시간 성공 횟수", example = "59")
        private Long successCount;

        @Schema(description = "해당 시간 실패 횟수", example = "1")
        private Long failureCount;

        @Schema(description = "해당 시간 평균 응답 시간", example = "145.5")
        private Double averageResponseTime;

        @Schema(description = "해당 시간 가용성", example = "98.3")
        private Double availability;
    }

    public Date getCalculatedAtAsDate() {
        return calculatedAt != null ? Date.from(calculatedAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public Date getPeriodStartAsDate() {
        return periodStart != null ? Date.from(periodStart.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public Date getPeriodEndAsDate() {
        return periodEnd != null ? Date.from(periodEnd.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}