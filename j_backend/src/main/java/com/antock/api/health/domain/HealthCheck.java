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
        @Index(name = "idx_health_checks_created_at", columnList = "created_at")
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
}
