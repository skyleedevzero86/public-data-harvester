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
@Schema(description = "페이징된 시스템 헬스 응답 DTO")
public class PagedSystemHealthResponse {

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

    @Schema(description = "현재 페이지의 컴포넌트 상태")
    private List<HealthCheckResponse> components;

    @Schema(description = "페이징 정보")
    private PaginationInfo pagination;

    @Schema(description = "컴포넌트 그룹 정보")
    private Map<String, ComponentGroupInfo> componentGroups;

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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이징 정보")
    public static class PaginationInfo {
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int pageNumber;

        @Schema(description = "페이지 크기", example = "10")
        private int pageSize;

        @Schema(description = "전체 요소 수", example = "25")
        private long totalElements;

        @Schema(description = "전체 페이지 수", example = "3")
        private int totalPages;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        private boolean hasPrevious;

        @Schema(description = "다음 페이지 번호", example = "1")
        private Integer nextPage;

        @Schema(description = "이전 페이지 번호", example = "0")
        private Integer previousPage;

        @Schema(description = "현재 페이지의 요소 수", example = "10")
        private int numberOfElements;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "컴포넌트 그룹 정보")
    public static class ComponentGroupInfo {
        @Schema(description = "그룹명", example = "redis")
        private String groupName;

        @Schema(description = "그룹 내 총 컴포넌트 수", example = "3")
        private int totalCount;

        @Schema(description = "정상 컴포넌트 수", example = "1")
        private int healthyCount;

        @Schema(description = "장애 컴포넌트 수", example = "2")
        private int unhealthyCount;

        @Schema(description = "그룹 상태", example = "DOWN")
        private HealthStatus groupStatus;

        @Schema(description = "그룹 상태 설명", example = "일부 장애")
        private String groupStatusDescription;
    }

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

    public static PagedSystemHealthResponseBuilder builder() {
        return new PagedSystemHealthResponseBuilder();
    }

    public static class PagedSystemHealthResponseBuilder {
        private HealthStatus overallStatus;
        private String overallStatusDescription;
        private Integer totalComponents;
        private Integer healthyComponents;
        private Integer unhealthyComponents;
        private Integer unknownComponents;
        private Double healthPercentage;
        private Map<String, Object> details;
        private List<HealthCheckResponse> components;
        private PaginationInfo pagination;
        private Map<String, ComponentGroupInfo> componentGroups;
        private LocalDateTime checkedAt;
        private LocalDateTime expiresAt;
        private boolean expired;
        private boolean healthy;

        public PagedSystemHealthResponseBuilder overallStatus(HealthStatus overallStatus) {
            this.overallStatus = overallStatus;
            return this;
        }

        public PagedSystemHealthResponseBuilder overallStatusDescription(String overallStatusDescription) {
            this.overallStatusDescription = overallStatusDescription;
            return this;
        }

        public PagedSystemHealthResponseBuilder totalComponents(Integer totalComponents) {
            this.totalComponents = totalComponents;
            return this;
        }

        public PagedSystemHealthResponseBuilder healthyComponents(Integer healthyComponents) {
            this.healthyComponents = healthyComponents;
            return this;
        }

        public PagedSystemHealthResponseBuilder unhealthyComponents(Integer unhealthyComponents) {
            this.unhealthyComponents = unhealthyComponents;
            return this;
        }

        public PagedSystemHealthResponseBuilder unknownComponents(Integer unknownComponents) {
            this.unknownComponents = unknownComponents;
            return this;
        }

        public PagedSystemHealthResponseBuilder healthPercentage(Double healthPercentage) {
            this.healthPercentage = healthPercentage;
            return this;
        }

        public PagedSystemHealthResponseBuilder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public PagedSystemHealthResponseBuilder components(List<HealthCheckResponse> components) {
            this.components = components;
            return this;
        }

        public PagedSystemHealthResponseBuilder pagination(PaginationInfo pagination) {
            this.pagination = pagination;
            return this;
        }

        public PagedSystemHealthResponseBuilder componentGroups(Map<String, ComponentGroupInfo> componentGroups) {
            this.componentGroups = componentGroups;
            return this;
        }

        public PagedSystemHealthResponseBuilder checkedAt(LocalDateTime checkedAt) {
            this.checkedAt = checkedAt;
            return this;
        }

        public PagedSystemHealthResponseBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public PagedSystemHealthResponseBuilder expired(boolean expired) {
            this.expired = expired;
            return this;
        }

        public PagedSystemHealthResponseBuilder healthy(boolean healthy) {
            this.healthy = healthy;
            return this;
        }

        public PagedSystemHealthResponse build() {
            PagedSystemHealthResponse response = new PagedSystemHealthResponse();
            response.overallStatus = this.overallStatus;
            response.overallStatusDescription = this.overallStatusDescription;
            response.totalComponents = this.totalComponents;
            response.healthyComponents = this.healthyComponents;
            response.unhealthyComponents = this.unhealthyComponents;
            response.unknownComponents = this.unknownComponents;
            response.healthPercentage = this.healthPercentage;
            response.details = this.details;
            response.components = this.components;
            response.pagination = this.pagination;
            response.componentGroups = this.componentGroups;
            response.checkedAt = this.checkedAt;
            response.expiresAt = this.expiresAt;
            response.expired = this.expired;
            response.healthy = this.healthy;
            return response;
        }
    }
}