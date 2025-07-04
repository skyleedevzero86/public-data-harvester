package com.antock.api.member.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_password_history")
@Getter
@NoArgsConstructor
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

    @Builder
    public MemberPasswordHistory(Member member, String passwordHash) {
        this.member = member;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }
}