package com.antock.api.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_password_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String action;

    @Column
    private String result;

    @Column
    private String message;

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    @Column
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public MemberPasswordHistory(Member member, String passwordHash) {
        this.member = member;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    public Long getMemberId() {
        return this.member != null ? this.member.getId() : null;
    }
}