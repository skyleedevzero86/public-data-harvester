package com.antock.api.member.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordStatusResponse {

    private boolean isChangeRequired;

    private boolean isChangeRecommended;

    private long todayChangeCount;

    private Long daysSinceLastChange;

    private Long daysUntilExpiry;

    private int maxDailyChanges;

    private Integer strengthScore;

    private String statusMessage;

    public static PasswordStatusResponse of(boolean isChangeRequired,
                                            boolean isChangeRecommended,
                                            long todayChangeCount) {
        return PasswordStatusResponse.builder()
                .isChangeRequired(isChangeRequired)
                .isChangeRecommended(isChangeRecommended)
                .todayChangeCount(todayChangeCount)
                .maxDailyChanges(3)
                .build();
    }

    public static PasswordStatusResponse of(boolean isChangeRequired,
                                            boolean isChangeRecommended,
                                            long todayChangeCount,
                                            Long daysSinceLastChange,
                                            Long daysUntilExpiry,
                                            int maxDailyChanges,
                                            Integer strengthScore,
                                            String statusMessage) {
        return PasswordStatusResponse.builder()
                .isChangeRequired(isChangeRequired)
                .isChangeRecommended(isChangeRecommended)
                .todayChangeCount(todayChangeCount)
                .daysSinceLastChange(daysSinceLastChange)
                .daysUntilExpiry(daysUntilExpiry)
                .maxDailyChanges(maxDailyChanges)
                .strengthScore(strengthScore)
                .statusMessage(statusMessage)
                .build();
    }

    public boolean canChangeToday() {
        return todayChangeCount < maxDailyChanges;
    }

    public int getRemainingChangesToday() {
        return Math.max(0, maxDailyChanges - (int) todayChangeCount);
    }

    public boolean isPasswordHealthy() {
        return !isChangeRequired && !isChangeRecommended;
    }

    public boolean isChangeRequired() {
        return isChangeRequired;
    }

    public boolean isChangeRecommended() {
        return isChangeRecommended;
    }

    public long getTodayChangeCount() {
        return todayChangeCount;
    }

    public Long getDaysSinceLastChange() {
        return daysSinceLastChange;
    }

    public Long getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public int getMaxDailyChanges() {
        return maxDailyChanges;
    }

    public Integer getStrengthScore() {
        return strengthScore;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public static PasswordStatusResponseBuilder builder() {
        return new PasswordStatusResponseBuilder();
    }

    public static class PasswordStatusResponseBuilder {
        private boolean isChangeRequired;
        private boolean isChangeRecommended;
        private long todayChangeCount;
        private Long daysSinceLastChange;
        private Long daysUntilExpiry;
        private int maxDailyChanges;
        private Integer strengthScore;
        private String statusMessage;

        public PasswordStatusResponseBuilder isChangeRequired(boolean isChangeRequired) {
            this.isChangeRequired = isChangeRequired;
            return this;
        }

        public PasswordStatusResponseBuilder isChangeRecommended(boolean isChangeRecommended) {
            this.isChangeRecommended = isChangeRecommended;
            return this;
        }

        public PasswordStatusResponseBuilder todayChangeCount(long todayChangeCount) {
            this.todayChangeCount = todayChangeCount;
            return this;
        }

        public PasswordStatusResponseBuilder daysSinceLastChange(Long daysSinceLastChange) {
            this.daysSinceLastChange = daysSinceLastChange;
            return this;
        }

        public PasswordStatusResponseBuilder daysUntilExpiry(Long daysUntilExpiry) {
            this.daysUntilExpiry = daysUntilExpiry;
            return this;
        }

        public PasswordStatusResponseBuilder maxDailyChanges(int maxDailyChanges) {
            this.maxDailyChanges = maxDailyChanges;
            return this;
        }

        public PasswordStatusResponseBuilder strengthScore(Integer strengthScore) {
            this.strengthScore = strengthScore;
            return this;
        }

        public PasswordStatusResponseBuilder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public PasswordStatusResponse build() {
            PasswordStatusResponse response = new PasswordStatusResponse();
            response.isChangeRequired = this.isChangeRequired;
            response.isChangeRecommended = this.isChangeRecommended;
            response.todayChangeCount = this.todayChangeCount;
            response.daysSinceLastChange = this.daysSinceLastChange;
            response.daysUntilExpiry = this.daysUntilExpiry;
            response.maxDailyChanges = this.maxDailyChanges;
            response.strengthScore = this.strengthScore;
            response.statusMessage = this.statusMessage;
            return response;
        }
    }
}