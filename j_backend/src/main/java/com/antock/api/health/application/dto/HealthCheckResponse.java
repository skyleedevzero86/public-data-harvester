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
}
