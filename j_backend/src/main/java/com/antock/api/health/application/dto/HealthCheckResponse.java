package com.antock.api.health.application.dto;

import com.antock.api.health.domain.HealthStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "헬스 체크 응답 DTO")
public class HealthCheckResponse {

    @Schema(description = "컴포넌트명", example = "database")
    private String component;

    @Schema(description = "상태", example = "UP")
    private HealthStatus status;

    @Schema(description = "상태 설명", example = "정상")
    private String statusDescription;

    @Schema(description = "메시지", example = "데이터베이스 연결 정상")
    private String message;

    @Schema(description = "응답 시간 (밀리초)", example = "150")
    private Long responseTime;

    @Schema(description = "체크 타입", example = "scheduled")
    private String checkType;

    @Schema(description = "추가 정보")
    private Map<String, Object> details;

    @Schema(description = "체크 실행 시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkedAt;

    @Schema(description = "만료 시간")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    @Schema(description = "만료 여부", example = "false")
    private boolean expired;

    @Schema(description = "정상 여부", example = "true")
    private boolean healthy;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isHealthy() {
        return HealthStatus.UP.equals(status);
    }

    public Date getCheckedAtAsDate() {
        return checkedAt != null ? Date.from(checkedAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public Date getExpiresAtAsDate() {
        return expiresAt != null ? Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public String getComponent() {
        return component;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public String getMessage() {
        return message;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public String getCheckType() {
        return checkType;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public static HealthCheckResponseBuilder builder() {
        return new HealthCheckResponseBuilder();
    }

    public static class HealthCheckResponseBuilder {
        private String component;
        private HealthStatus status;
        private String statusDescription;
        private String message;
        private Long responseTime;
        private String checkType;
        private Map<String, Object> details;
        private LocalDateTime checkedAt;
        private LocalDateTime expiresAt;
        private boolean expired;
        private boolean healthy;

        public HealthCheckResponseBuilder component(String component) {
            this.component = component;
            return this;
        }

        public HealthCheckResponseBuilder status(HealthStatus status) {
            this.status = status;
            return this;
        }

        public HealthCheckResponseBuilder statusDescription(String statusDescription) {
            this.statusDescription = statusDescription;
            return this;
        }

        public HealthCheckResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public HealthCheckResponseBuilder responseTime(Long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public HealthCheckResponseBuilder checkType(String checkType) {
            this.checkType = checkType;
            return this;
        }

        public HealthCheckResponseBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public HealthCheckResponseBuilder checkedAt(LocalDateTime checkedAt) {
            this.checkedAt = checkedAt;
            return this;
        }

        public HealthCheckResponseBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public HealthCheckResponseBuilder expired(boolean expired) {
            this.expired = expired;
            return this;
        }

        public HealthCheckResponseBuilder healthy(boolean healthy) {
            this.healthy = healthy;
            return this;
        }

        public HealthCheckResponse build() {
            HealthCheckResponse response = new HealthCheckResponse();
            response.component = this.component;
            response.status = this.status;
            response.statusDescription = this.statusDescription;
            response.message = this.message;
            response.responseTime = this.responseTime;
            response.checkType = this.checkType;
            response.details = this.details;
            response.checkedAt = this.checkedAt;
            response.expiresAt = this.expiresAt;
            response.expired = this.expired;
            response.healthy = this.healthy;
            return response;
        }
    }
}