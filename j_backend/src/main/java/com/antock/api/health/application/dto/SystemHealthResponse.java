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
@Schema(description = "시스템 헬스 응답 DTO")
public class SystemHealthResponse {

    @Schema(description = "전체 상태", example = "UP")
    private HealthStatus overallStatus;

    @Schema(description = "전체 상태 설명", example = "정상")
    private String overallStatusDescription;

    @Schema(description = "총 컴포넌트 수", example = "5")
    private Integer totalComponents;

    @Schema(description = "정상 컴포넌트 수", example = "4")
    private Integer healthyComponents;

    @Schema(description = "장애 컴포넌트 수", example = "1")
    private Integer unhealthyComponents;

    @Schema(description = "알 수 없는 컴포넌트 수", example = "0")
    private Integer unknownComponents;

    @Schema(description = "헬스 비율", example = "80.0")
    private Double healthPercentage;

    @Schema(description = "상세 정보")
    private Map<String, Object> details;

    @Schema(description = "개별 컴포넌트 상태")
    private List<HealthCheckResponse> components;

    @Schema(description = "체크 실행 시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkedAt;

    @Schema(description = "만료 시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    @Schema(description = "만료 여부", example = "false")
    private boolean expired;

    @Schema(description = "전체 정상 여부", example = "true")
    private boolean healthy;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isHealthy() {
        return HealthStatus.UP.equals(overallStatus);
    }

    public Double getHealthPercentage() {
        if (totalComponents == null || totalComponents == 0) {
            return 0.0;
        }
        return (double) healthyComponents / totalComponents * 100;
    }

    public Date getCheckedAtAsDate() {
        return checkedAt != null ? Date.from(checkedAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public Date getExpiresAtAsDate() {
        return expiresAt != null ? Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public HealthStatus getOverallStatus() {
        return overallStatus;
    }

    public String getOverallStatusDescription() {
        return overallStatusDescription;
    }

    public Integer getTotalComponents() {
        return totalComponents;
    }

    public Integer getHealthyComponents() {
        return healthyComponents;
    }

    public Integer getUnhealthyComponents() {
        return unhealthyComponents;
    }

    public Integer getUnknownComponents() {
        return unknownComponents;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public List<HealthCheckResponse> getComponents() {
        return components;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public static SystemHealthResponseBuilder builder() {
        return new SystemHealthResponseBuilder();
    }

    public static class SystemHealthResponseBuilder {
        private HealthStatus overallStatus;
        private String overallStatusDescription;
        private Integer totalComponents;
        private Integer healthyComponents;
        private Integer unhealthyComponents;
        private Integer unknownComponents;
        private Double healthPercentage;
        private Map<String, Object> details;
        private List<HealthCheckResponse> components;
        private LocalDateTime checkedAt;
        private LocalDateTime expiresAt;
        private boolean expired;
        private boolean healthy;

        public SystemHealthResponseBuilder overallStatus(HealthStatus overallStatus) {
            this.overallStatus = overallStatus;
            return this;
        }

        public SystemHealthResponseBuilder overallStatusDescription(String overallStatusDescription) {
            this.overallStatusDescription = overallStatusDescription;
            return this;
        }

        public SystemHealthResponseBuilder totalComponents(Integer totalComponents) {
            this.totalComponents = totalComponents;
            return this;
        }

        public SystemHealthResponseBuilder healthyComponents(Integer healthyComponents) {
            this.healthyComponents = healthyComponents;
            return this;
        }

        public SystemHealthResponseBuilder unhealthyComponents(Integer unhealthyComponents) {
            this.unhealthyComponents = unhealthyComponents;
            return this;
        }

        public SystemHealthResponseBuilder unknownComponents(Integer unknownComponents) {
            this.unknownComponents = unknownComponents;
            return this;
        }

        public SystemHealthResponseBuilder healthPercentage(Double healthPercentage) {
            this.healthPercentage = healthPercentage;
            return this;
        }

        public SystemHealthResponseBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public SystemHealthResponseBuilder components(List<HealthCheckResponse> components) {
            this.components = components;
            return this;
        }

        public SystemHealthResponseBuilder checkedAt(LocalDateTime checkedAt) {
            this.checkedAt = checkedAt;
            return this;
        }

        public SystemHealthResponseBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public SystemHealthResponseBuilder expired(boolean expired) {
            this.expired = expired;
            return this;
        }

        public SystemHealthResponseBuilder healthy(boolean healthy) {
            this.healthy = healthy;
            return this;
        }

        public SystemHealthResponse build() {
            SystemHealthResponse response = new SystemHealthResponse();
            response.overallStatus = this.overallStatus;
            response.overallStatusDescription = this.overallStatusDescription;
            response.totalComponents = this.totalComponents;
            response.healthyComponents = this.healthyComponents;
            response.unhealthyComponents = this.unhealthyComponents;
            response.unknownComponents = this.unknownComponents;
            response.healthPercentage = this.healthPercentage;
            response.details = this.details;
            response.components = this.components;
            response.checkedAt = this.checkedAt;
            response.expiresAt = this.expiresAt;
            response.expired = this.expired;
            response.healthy = this.healthy;
            return response;
        }
    }
}