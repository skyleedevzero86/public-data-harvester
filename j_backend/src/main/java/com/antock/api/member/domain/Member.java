package com.antock.api.member.domain;

import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_username", columnList = "username"),
        @Index(name = "idx_member_email", columnList = "email"),
        @Index(name = "idx_member_api_key", columnList = "apiKey")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseTimeEntity {

    @Column(unique = true, length = 50, nullable = false)
    private String username;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 64)
    private String apiKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column
    private LocalDateTime lastLoginAt;

    @Column
    @Builder.Default
    private Integer loginFailCount = 0;

    @Column
    private LocalDateTime accountLockedAt;

    @Column
    private Long approvedBy;

    @Column
    private LocalDateTime approvedAt;

    // 비즈니스 메서드들
    public String getName() {
        return nickname;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isApproved() {
        return status == MemberStatus.APPROVED;
    }

    public boolean isActive() {
        return status == MemberStatus.APPROVED && !isLocked();
    }

    public boolean isLocked() {
        return accountLockedAt != null &&
                accountLockedAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    public boolean matchPassword(String rawPassword) {
        // 실제로는 BCryptPasswordEncoder 사용
        return this.password.equals(rawPassword);
    }

    public void approve(Long approverId) {
        this.status = MemberStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = MemberStatus.REJECTED;
    }

    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    public void increaseLoginFailCount() {
        this.loginFailCount++;
        if (this.loginFailCount >= 5) {
            this.accountLockedAt = LocalDateTime.now();
        }
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.accountLockedAt = null;
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> authorities = new ArrayList<>();
        authorities.add("ROLE_" + role.name());

        if (isAdmin()) {
            authorities.add("ROLE_ADMIN");
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}