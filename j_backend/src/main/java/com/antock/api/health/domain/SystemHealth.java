package com.antock.api.health.domain;

import com.antock.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "system_health", indexes = {
        @Index(name = "idx_system_health_create_date", columnList = "create_date"),
        @Index(name = "idx_system_health_overall_status", columnList = "overall_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Comment("전체 시스템 헬스 상태")
public class SystemHealth extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("시스템 헬스 ID")
    private Long id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Comment("전체 상태")
    private HealthStatus overallStatus;

    @Column
    @Comment("총 컴포넌트 수")
    private Integer totalComponents;

    @Column
    @Comment("정상 컴포넌트 수")
    private Integer healthyComponents;

    @Column
    @Comment("장애 컴포넌트 수")
    private Integer unhealthyComponents;

    @Column
    @Comment("알 수 없는 컴포넌트 수")
    private Integer unknownComponents;

    @Column(length = 2000)
    @Comment("상세 정보 (JSON)")
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
            expiresAt = checkedAt.plusMinutes(10);
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isHealthy() {
        return HealthStatus.UP.equals(overallStatus);
    }

    public double getHealthPercentage() {
        if (totalComponents == null || totalComponents == 0) {
            return 0.0;
        }
        return (double) healthyComponents / totalComponents * 100;
    }

    public Integer getUnhealthyComponents() {
        return totalComponents - healthyComponents - (unknownComponents != null ? unknownComponents : 0);
    }
}
