package com.antock.api.member.domain;

import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@Slf4j
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

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "password_change_count")
    @Builder.Default
    private int passwordChangeCount = 0;

    @Column(name = "last_password_change_date")
    private LocalDate lastPasswordChangeDate;

    public void changePassword(String newPassword) {
        log.info("비밀번호 변경 실행 - memberId: {}, 이전 passwordChangedAt: {}",
                getId(), this.passwordChangedAt);

        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();

        LocalDate today = LocalDate.now();
        if (this.lastPasswordChangeDate == null || !this.lastPasswordChangeDate.equals(today)) {
            this.lastPasswordChangeDate = today;
            this.passwordChangeCount = 1;
        } else {
            this.passwordChangeCount++;
        }

        log.info("비밀번호 변경 완료 - memberId: {}, 새로운 passwordChangedAt: {}, 오늘 변경 횟수: {}",
                getId(), this.passwordChangedAt, this.passwordChangeCount);
    }

    public boolean canChangePassword() {
        LocalDate today = LocalDate.now();
        if (this.lastPasswordChangeDate == null || !this.lastPasswordChangeDate.equals(today)) {
            return true;
        }
        boolean canChange = this.passwordChangeCount < 3;

        log.debug("비밀번호 변경 가능 여부 - memberId: {}, 오늘 변경 횟수: {}, 가능 여부: {}",
                getId(), this.passwordChangeCount, canChange);

        return canChange;
    }

    public boolean isPasswordChangeRequired() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDate;

        if (this.passwordChangedAt == null) {
            baseDate = this.getCreateDate();
            log.debug("비밀번호 변경 필요 여부 확인 (가입일 기준) - memberId: {}, 가입일: {}",
                    getId(), baseDate);
        } else {
            baseDate = this.passwordChangedAt;
            log.debug("비밀번호 변경 필요 여부 확인 (마지막 변경일 기준) - memberId: {}, 마지막 변경일: {}",
                    getId(), baseDate);
        }

        if (baseDate == null) {
            log.warn("기준 날짜가 null입니다 - memberId: {}", getId());
            return false;
        }

        boolean isRequired = baseDate.isBefore(now.minusDays(90));

        log.info("비밀번호 변경 필요 여부 - memberId: {}, 기준일: {}, 90일 전: {}, 필요 여부: {}",
                getId(), baseDate, now.minusDays(90), isRequired);

        return isRequired;
    }

    public boolean isPasswordChangeRecommended() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDate;

        if (this.passwordChangedAt == null) {
            baseDate = this.getCreateDate();
        } else {
            baseDate = this.passwordChangedAt;
        }

        if (baseDate == null) {
            return false;
        }

        boolean isRecommended = baseDate.isBefore(now.minusDays(80));

        log.debug("비밀번호 변경 권장 여부 - memberId: {}, 기준일: {}, 80일 전: {}, 권장 여부: {}",
                getId(), baseDate, now.minusDays(80), isRecommended);

        return isRecommended;
    }

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
        return accountLockedAt != null && accountLockedAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    public boolean matchPassword(String rawPassword) {
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
            this.status = MemberStatus.SUSPENDED;
        }
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.accountLockedAt = null;
        if (this.status == MemberStatus.SUSPENDED) {
            this.status = MemberStatus.APPROVED;
        }
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

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.email = maskEmail(this.email);
    }

    private String maskEmail(String email) {
        int idx = email.indexOf("@");
        if (idx > 1) {
            return email.charAt(0) + "***" + email.substring(idx);
        }
        return "***";
    }
}