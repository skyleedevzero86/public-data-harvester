package com.antock.api.member.domain;

import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.entity.BaseTimeEntity;
import com.antock.global.utils.PasswordUtils;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
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
        if (this.passwordChangedAt == null) {
            log.debug("비밀번호 변경 필요 - 최초 변경 안함: memberId={}", getId());
            return true;
        }

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        boolean isRequired = this.passwordChangedAt.isBefore(threeMonthsAgo);

        log.debug("비밀번호 변경 필요 여부 - memberId: {}, passwordChangedAt: {}, 3개월 전: {}, 필요 여부: {}",
                getId(), this.passwordChangedAt, threeMonthsAgo, isRequired);

        return isRequired;
    }

    public boolean isPasswordChangeRecommended() {
        if (this.passwordChangedAt == null) {
            log.debug("비밀번호 변경 권장 - 최초 변경 안함: memberId={}", getId());
            return true;
        }

        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        boolean isRecommended = this.passwordChangedAt.isBefore(twoMonthsAgo);

        log.debug("비밀번호 변경 권장 여부 - memberId: {}, passwordChangedAt: {}, 2개월 전: {}, 권장 여부: {}",
                getId(), this.passwordChangedAt, twoMonthsAgo, isRecommended);

        return isRecommended;
    }

    public String getName() {
        return this.username;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isApproved() {
        return this.status == MemberStatus.APPROVED;
    }

    public boolean isActive() {
        return this.status == MemberStatus.APPROVED && !isLocked();
    }

    public boolean isLocked() {
        return this.loginFailCount >= 5 || this.status == MemberStatus.SUSPENDED;
    }

    public boolean matchPassword(String rawPassword) {
        return PasswordUtils.matches(rawPassword, this.password);
    }

    public void approve(Long approverId) {
        this.status = MemberStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
        this.loginFailCount = 0;
        this.accountLockedAt = null;
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
            this.status = MemberStatus.SUSPENDED;
            this.accountLockedAt = LocalDateTime.now();
            log.warn("계정이 잠김 - memberId: {}, failCount: {}, lockedAt: {}",
                    getId(), this.loginFailCount, this.accountLockedAt);
        }

        log.debug("로그인 실패 카운트 증가 - memberId: {}, failCount: {}, status: {}",
                getId(), this.loginFailCount, this.status);
    }

    public void resetLoginFailCount() {
        log.debug("로그인 실패 카운트 리셋 전 - memberId: {}, 이전 failCount: {}, 이전 status: {}",
                getId(), this.loginFailCount, this.status);

        this.loginFailCount = 0;
        if (this.status == MemberStatus.SUSPENDED && this.accountLockedAt != null) {
            this.status = MemberStatus.APPROVED;
            this.accountLockedAt = null;
        }

        log.info("로그인 실패 카운트 리셋 완료 - memberId: {}, 새로운 failCount: {}, 새로운 status: {}",
                getId(), this.loginFailCount, this.status);
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
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.role.name()));

        if (this.role == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else if (this.role == Role.MANAGER) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.email = maskEmail(this.email);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        String maskedLocal = localPart.length() > 2 ?
                localPart.substring(0, 2) + "***" : "***";
        String maskedDomain = "***." + (domainPart.contains(".") ?
                domainPart.substring(domainPart.lastIndexOf(".") + 1) : "***");

        return maskedLocal + "@" + maskedDomain;
    }

    public void resetToPending() {
        this.status = MemberStatus.PENDING;
        this.approvedBy = null;
        this.approvedAt = null;
        this.accountLockedAt = null;
        this.loginFailCount = 0;
        log.info("회원 상태를 승인 대기로 재설정: memberId={}, username={}", this.getId(), this.username);
    }
}