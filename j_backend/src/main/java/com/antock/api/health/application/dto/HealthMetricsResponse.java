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

    public Double getOverallAvailability() {
        return overallAvailability;
    }

    public Double getAverageResponseTime() {
        return averageResponseTime;
    }

    public Double getMaxResponseTime() {
        return maxResponseTime;
    }

    public Double getMinResponseTime() {
        return minResponseTime;
    }

    public Long getTotalChecks() {
        return totalChecks;
    }

    public Long getSuccessfulChecks() {
        return successfulChecks;
    }

    public Long getFailedChecks() {
        return failedChecks;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public List<ComponentMetrics> getComponentMetrics() {
        return componentMetrics;
    }

    public List<TimeBasedMetrics> getTimeBasedMetrics() {
        return timeBasedMetrics;
    }

    public Map<HealthStatus, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public List<HourlyMetrics> getHourlyTrends() {
        return hourlyTrends;
    }

    public Double getAverageUptime() {
        return averageUptime;
    }

    public Double getAverageDowntime() {
        return averageDowntime;
    }

    public Double getMtbf() {
        return mtbf;
    }

    public Double getMttr() {
        return mttr;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public static HealthMetricsResponseBuilder builder() {
        return new HealthMetricsResponseBuilder();
    }

    public static class HealthMetricsResponseBuilder {
        private Double overallAvailability;
        private Double averageResponseTime;
        private Double maxResponseTime;
        private Double minResponseTime;
        private Long totalChecks;
        private Long successfulChecks;
        private Long failedChecks;
        private Double successRate;
        private List<ComponentMetrics> componentMetrics;
        private List<TimeBasedMetrics> timeBasedMetrics;
        private Map<HealthStatus, Long> statusDistribution;
        private List<HourlyMetrics> hourlyTrends;
        private Double averageUptime;
        private Double averageDowntime;
        private Double mtbf;
        private Double mttr;
        private LocalDateTime calculatedAt;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;

        public HealthMetricsResponseBuilder overallAvailability(Double overallAvailability) {
            this.overallAvailability = overallAvailability;
            return this;
        }

        public HealthMetricsResponseBuilder averageResponseTime(Double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public HealthMetricsResponseBuilder maxResponseTime(Double maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
            return this;
        }

        public HealthMetricsResponseBuilder minResponseTime(Double minResponseTime) {
            this.minResponseTime = minResponseTime;
            return this;
        }

        public HealthMetricsResponseBuilder totalChecks(Long totalChecks) {
            this.totalChecks = totalChecks;
            return this;
        }

        public HealthMetricsResponseBuilder successfulChecks(Long successfulChecks) {
            this.successfulChecks = successfulChecks;
            return this;
        }

        public HealthMetricsResponseBuilder failedChecks(Long failedChecks) {
            this.failedChecks = failedChecks;
            return this;
        }

        public HealthMetricsResponseBuilder successRate(Double successRate) {
            this.successRate = successRate;
            return this;
        }

        public HealthMetricsResponseBuilder componentMetrics(List<ComponentMetrics> componentMetrics) {
            this.componentMetrics = componentMetrics;
            return this;
        }

        public HealthMetricsResponseBuilder timeBasedMetrics(List<TimeBasedMetrics> timeBasedMetrics) {
            this.timeBasedMetrics = timeBasedMetrics;
            return this;
        }

        public HealthMetricsResponseBuilder statusDistribution(Map<HealthStatus, Long> statusDistribution) {
            this.statusDistribution = statusDistribution;
            return this;
        }

        public HealthMetricsResponseBuilder hourlyTrends(List<HourlyMetrics> hourlyTrends) {
            this.hourlyTrends = hourlyTrends;
            return this;
        }

        public HealthMetricsResponseBuilder averageUptime(Double averageUptime) {
            this.averageUptime = averageUptime;
            return this;
        }

        public HealthMetricsResponseBuilder averageDowntime(Double averageDowntime) {
            this.averageDowntime = averageDowntime;
            return this;
        }

        public HealthMetricsResponseBuilder mtbf(Double mtbf) {
            this.mtbf = mtbf;
            return this;
        }

        public HealthMetricsResponseBuilder mttr(Double mttr) {
            this.mttr = mttr;
            return this;
        }

        public HealthMetricsResponseBuilder calculatedAt(LocalDateTime calculatedAt) {
            this.calculatedAt = calculatedAt;
            return this;
        }

        public HealthMetricsResponseBuilder periodStart(LocalDateTime periodStart) {
            this.periodStart = periodStart;
            return this;
        }

        public HealthMetricsResponseBuilder periodEnd(LocalDateTime periodEnd) {
            this.periodEnd = periodEnd;
            return this;
        }

        public HealthMetricsResponse build() {
            HealthMetricsResponse response = new HealthMetricsResponse();
            response.overallAvailability = this.overallAvailability;
            response.averageResponseTime = this.averageResponseTime;
            response.maxResponseTime = this.maxResponseTime;
            response.minResponseTime = this.minResponseTime;
            response.totalChecks = this.totalChecks;
            response.successfulChecks = this.successfulChecks;
            response.failedChecks = this.failedChecks;
            response.successRate = this.successRate;
            response.componentMetrics = this.componentMetrics;
            response.timeBasedMetrics = this.timeBasedMetrics;
            response.statusDistribution = this.statusDistribution;
            response.hourlyTrends = this.hourlyTrends;
            response.averageUptime = this.averageUptime;
            response.averageDowntime = this.averageDowntime;
            response.mtbf = this.mtbf;
            response.mttr = this.mttr;
            response.calculatedAt = this.calculatedAt;
            response.periodStart = this.periodStart;
            response.periodEnd = this.periodEnd;
            return response;
        }
    }
}