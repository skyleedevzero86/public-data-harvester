package com.antock.api.health.domain;

import com.antock.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_checks", indexes = {
        @Index(name = "idx_health_checks_component", columnList = "component"),
        @Index(name = "idx_health_checks_status", columnList = "status"),
        @Index(name = "idx_health_checks_create_date", columnList = "create_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Comment("시스템 헬스 체크 정보")
public class HealthCheck extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("헬스 체크 ID")
    private Long id;

    @Column(nullable = false, length = 50)
    @Comment("컴포넌트명 (database, redis, cache, api, etc.)")
    private String component;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Comment("상태 (UP, DOWN, UNKNOWN)")
    private HealthStatus status;

    @Column(length = 1000)
    @Comment("상세 메시지")
    private String message;

    @Column
    @Comment("응답 시간 (밀리초)")
    private Long responseTime;

    @Column(length = 100)
    @Comment("체크 타입 (manual, scheduled, api)")
    private String checkType;

    @Column(length = 200)
    @Comment("추가 정보 (JSON 형태)")
    private String details;

    @Column
    @Comment("체크 실행 시간")
    private LocalDateTime checkedAt;

    @Column
    @Comment("만료 시간")
    private LocalDateTime expiresAt;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (checkedAt == null) {
            checkedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = checkedAt.plusMinutes(5);
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUp() {
        return HealthStatus.UP.equals(status);
    }

    public boolean isDown() {
        return HealthStatus.DOWN.equals(status);
    }

    public boolean isHealthy() {
        return isUp();
    }

    public String getComponent() {
        return component;
    }

    public HealthStatus getStatus() {
        return status;
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

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public static HealthCheckBuilder builder() {
        return new HealthCheckBuilder();
    }

    public static class HealthCheckBuilder {
        private Long id;
        private String component;
        private HealthStatus status;
        private String message;
        private Long responseTime;
        private String checkType;
        private String details;
        private LocalDateTime checkedAt;
        private LocalDateTime expiresAt;

        public HealthCheckBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public HealthCheckBuilder component(String component) {
            this.component = component;
            return this;
        }

        public HealthCheckBuilder status(HealthStatus status) {
            this.status = status;
            return this;
        }

        public HealthCheckBuilder message(String message) {
            this.message = message;
            return this;
        }

        public HealthCheckBuilder responseTime(Long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public HealthCheckBuilder checkType(String checkType) {
            this.checkType = checkType;
            return this;
        }

        public HealthCheckBuilder details(String details) {
            this.details = details;
            return this;
        }

        public HealthCheckBuilder checkedAt(LocalDateTime checkedAt) {
            this.checkedAt = checkedAt;
            return this;
        }

        public HealthCheckBuilder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public HealthCheck build() {
            HealthCheck healthCheck = new HealthCheck();
            healthCheck.id = this.id;
            healthCheck.component = this.component;
            healthCheck.status = this.status;
            healthCheck.message = this.message;
            healthCheck.responseTime = this.responseTime;
            healthCheck.checkType = this.checkType;
            healthCheck.details = this.details;
            healthCheck.checkedAt = this.checkedAt;
            healthCheck.expiresAt = this.expiresAt;
            return healthCheck;
        }
    }
}