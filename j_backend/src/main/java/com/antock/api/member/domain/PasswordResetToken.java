package com.antock.api.member.domain;

import com.antock.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token", indexes = {
        @Index(name = "idx_password_reset_token_token", columnList = "token"),
        @Index(name = "idx_password_reset_token_member_id", columnList = "member_id"),
        @Index(name = "idx_password_reset_token_expires_at", columnList = "expires_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = { "member" })
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PasswordResetToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 45)
    private String clientIp;

    @Column(length = 500)
    private String userAgent;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public void markAsUsed() {
        this.used = true;
    }

    public Long getMemberId() {
        return this.member != null ? this.member.getId() : null;
    }

    public static PasswordResetToken create(Member member, String token, LocalDateTime expiresAt, String clientIp,
            String userAgent) {
        return PasswordResetToken.builder()
                .member(member)
                .token(token)
                .expiresAt(expiresAt)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();
    }

}