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
    private Integer passwordChangeCount = 0;

    @Column(name = "last_password_change_date")
    private LocalDate lastPasswordChangeDate;

    public void changePassword(String newPassword) {
        log.info("비밀번호 변경 실행 - memberId: {}, 이전 passwordChangedAt: {}",
                getId(), this.passwordChangedAt);

        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();

        LocalDate today = LocalDate.now();
        if (this.lastPasswordChangeDate == null || !this.lastPasswordChangeDate.equals(today)) {
            this.passwordChangeCount = 1;
            this.lastPasswordChangeDate = today;
        } else {
            this.passwordChangeCount = (this.passwordChangeCount == null ? 0 : this.passwordChangeCount) + 1;
        }

        log.info("비밀번호 변경 완료 - memberId: {}, passwordChangedAt: {}, 오늘 변경 횟수: {}",
                getId(), this.passwordChangedAt, this.passwordChangeCount);
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();

        LocalDate today = LocalDate.now();
        if (this.lastPasswordChangeDate == null || !this.lastPasswordChangeDate.equals(today)) {
            this.passwordChangeCount = 1;
            this.lastPasswordChangeDate = today;
        } else {
            this.passwordChangeCount = (this.passwordChangeCount == null ? 0 : this.passwordChangeCount) + 1;
        }

        log.info("비밀번호 업데이트 완료 - memberId: {}, passwordChangedAt: {}", getId(), this.passwordChangedAt);
    }

    public boolean canChangePassword() {
        LocalDate today = LocalDate.now();
        if (this.lastPasswordChangeDate == null || !this.lastPasswordChangeDate.equals(today)) {
            return true;
        }

        int maxDailyChanges = 3;
        return this.passwordChangeCount == null || this.passwordChangeCount < maxDailyChanges;
    }

    public int getTodayPasswordChangeCount() {
        LocalDate today = LocalDate.now();
        if (this.lastPasswordChangeDate == null || !this.lastPasswordChangeDate.equals(today)) {
            return 0;
        }
        return this.passwordChangeCount == null ? 0 : this.passwordChangeCount;
    }

    public boolean isPasswordChangeRequired() {
        if (this.passwordChangedAt == null) {
            log.debug("비밀번호 변경 필요 - 최초 변경 안함: memberId={}", getId());
            return true;
        }

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        boolean isRequired = this.passwordChangedAt.isBefore(thirtyDaysAgo);

        log.debug("비밀번호 변경 필요 여부: memberId={}, passwordChangedAt={}, required={}",
                getId(), this.passwordChangedAt, isRequired);

        return isRequired;
    }

    public boolean isPasswordChangeRecommended() {
        if (this.passwordChangedAt == null) {
            log.debug("비밀번호 변경 권장 - 최초 변경 안함: memberId={}", getId());
            return true;
        }

        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
        boolean isRecommended = this.passwordChangedAt.isBefore(fourteenDaysAgo);

        log.debug("비밀번호 변경 권장 여부: memberId={}, passwordChangedAt={}, recommended={}",
                getId(), this.passwordChangedAt, isRecommended);

        return isRecommended;
    }

    public String getName() {
        return this.username;
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

    public boolean isActive() {
        boolean isStatusActive = this.status == MemberStatus.APPROVED;
        boolean isNotLocked = !isLocked();

        log.debug("회원 활성 상태 확인: memberId={}, status={}, isLocked={}, isActive={}",
                getId(), this.status, isLocked(), isStatusActive && isNotLocked);

        return isStatusActive && isNotLocked;
    }

    public boolean isLocked() {
        return this.loginFailCount >= 5 || this.status == MemberStatus.SUSPENDED;
    }

    public boolean matchPassword(String rawPassword) {
        return PasswordUtils.matches(rawPassword, this.password);
    }

    public void increaseLoginFailCount() {
        this.loginFailCount = (this.loginFailCount == null ? 0 : this.loginFailCount) + 1;

        if (this.loginFailCount >= 5) {
            this.status = MemberStatus.SUSPENDED;
            this.accountLockedAt = LocalDateTime.now();
            log.warn("계정 잠금 처리: memberId={}, loginFailCount={}", getId(), this.loginFailCount);
        }

        log.debug("로그인 실패 카운트 증가: memberId={}, loginFailCount={}", getId(), this.loginFailCount);
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.accountLockedAt = null;
        log.debug("로그인 실패 카운트 초기화: memberId={}", getId());
    }

    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
        log.info("프로필 업데이트: memberId={}, nickname={}, email={}", getId(), nickname, email);
    }

    public void approve(Long approverId) {
        this.status = MemberStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
        this.accountLockedAt = null;
        this.loginFailCount = 0;
        log.info("회원 승인: memberId={}, approverId={}, approvedAt={}", getId(), approverId, this.approvedAt);
    }

    public void reject() {
        this.status = MemberStatus.REJECTED;
        this.approvedBy = null;
        this.approvedAt = null;
        log.info("회원 거부: memberId={}", getId());
    }

    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
        this.accountLockedAt = LocalDateTime.now();
        log.info("회원 정지: memberId={}, accountLockedAt={}", getId(), this.accountLockedAt);
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        log.info("회원 탈퇴: memberId={}", getId());
    }

    public void changeRole(Role newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("역할은 null일 수 없습니다.");
        }

        if (this.role == Role.ADMIN && newRole != Role.ADMIN) {
            throw new IllegalArgumentException("ADMIN 역할에서 다른 역할로 변경할 수 없습니다.");
        }

        this.role = newRole;
    }

    public void unlock() {
        if (this.status == MemberStatus.SUSPENDED) {
            this.status = MemberStatus.APPROVED;
        }
        this.accountLockedAt = null;
        this.loginFailCount = 0;
        log.info("회원 잠금 해제: memberId={}", getId());
    }

    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        log.debug("마지막 로그인 시간 업데이트: memberId={}, lastLoginAt={}", getId(), lastLoginAt);
    }

    public String getMaskedEmail() {
        if (this.email == null || !this.email.contains("@")) {
            return this.email;
        }

        String[] parts = this.email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        String maskedLocal;
        if (localPart.length() <= 2) {
            maskedLocal = localPart.charAt(0) + "*";
        } else {
            maskedLocal = localPart.charAt(0) + "*".repeat(localPart.length() - 2)
                    + localPart.charAt(localPart.length() - 1);
        }

        String maskedDomain;
        if (domainPart.length() <= 2) {
            maskedDomain = "*" + domainPart.charAt(domainPart.length() - 1);
        } else {
            maskedDomain = domainPart.charAt(0) + "*".repeat(domainPart.length() - 2)
                    + domainPart.charAt(domainPart.length() - 1);
        }

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

    public Long getId() {
        return super.getId();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getApiKey() {
        return apiKey;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public Integer getLoginFailCount() {
        return loginFailCount;
    }

    public LocalDateTime getAccountLockedAt() {
        return accountLockedAt;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public Integer getPasswordChangeCount() {
        return passwordChangeCount;
    }

    public LocalDate getLastPasswordChangeDate() {
        return lastPasswordChangeDate;
    }

    public static MemberBuilder builder() {
        return new MemberBuilder();
    }

    public static class MemberBuilder {
        private String username;
        private String password;
        private String nickname;
        private String email;
        private String apiKey;
        private MemberStatus status = MemberStatus.PENDING;
        private Role role = Role.USER;
        private LocalDateTime lastLoginAt;
        private Integer loginFailCount = 0;
        private LocalDateTime accountLockedAt;
        private Long approvedBy;
        private LocalDateTime approvedAt;
        private LocalDateTime passwordChangedAt;
        private Integer passwordChangeCount = 0;
        private LocalDate lastPasswordChangeDate;

        public MemberBuilder username(String username) {
            this.username = username;
            return this;
        }

        public MemberBuilder password(String password) {
            this.password = password;
            return this;
        }

        public MemberBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public MemberBuilder email(String email) {
            this.email = email;
            return this;
        }

        public MemberBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public MemberBuilder status(MemberStatus status) {
            this.status = status;
            return this;
        }

        public MemberBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public MemberBuilder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public MemberBuilder loginFailCount(Integer loginFailCount) {
            this.loginFailCount = loginFailCount;
            return this;
        }

        public MemberBuilder accountLockedAt(LocalDateTime accountLockedAt) {
            this.accountLockedAt = accountLockedAt;
            return this;
        }

        public MemberBuilder approvedBy(Long approvedBy) {
            this.approvedBy = approvedBy;
            return this;
        }

        public MemberBuilder approvedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public MemberBuilder passwordChangedAt(LocalDateTime passwordChangedAt) {
            this.passwordChangedAt = passwordChangedAt;
            return this;
        }

        public MemberBuilder passwordChangeCount(Integer passwordChangeCount) {
            this.passwordChangeCount = passwordChangeCount;
            return this;
        }

        public MemberBuilder lastPasswordChangeDate(LocalDate lastPasswordChangeDate) {
            this.lastPasswordChangeDate = lastPasswordChangeDate;
            return this;
        }

        public Member build() {
            Member member = new Member();
            member.username = this.username;
            member.password = this.password;
            member.nickname = this.nickname;
            member.email = this.email;
            member.apiKey = this.apiKey;
            member.status = this.status;
            member.role = this.role;
            member.lastLoginAt = this.lastLoginAt;
            member.loginFailCount = this.loginFailCount;
            member.accountLockedAt = this.accountLockedAt;
            member.approvedBy = this.approvedBy;
            member.approvedAt = this.approvedAt;
            member.passwordChangedAt = this.passwordChangedAt;
            member.passwordChangeCount = this.passwordChangeCount;
            member.lastPasswordChangeDate = this.lastPasswordChangeDate;
            return member;
        }
    }
}