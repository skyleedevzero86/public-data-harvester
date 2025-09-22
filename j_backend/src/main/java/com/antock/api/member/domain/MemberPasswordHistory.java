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

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAction() {
        return action;
    }

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public static MemberPasswordHistoryBuilder builder() {
        return new MemberPasswordHistoryBuilder();
    }

    public static class MemberPasswordHistoryBuilder {
        private Long id;
        private Member member;
        private String passwordHash;
        private LocalDateTime createdAt;
        private String action;
        private String result;
        private String message;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;

        public MemberPasswordHistoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MemberPasswordHistoryBuilder member(Member member) {
            this.member = member;
            return this;
        }

        public MemberPasswordHistoryBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public MemberPasswordHistoryBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MemberPasswordHistoryBuilder action(String action) {
            this.action = action;
            return this;
        }

        public MemberPasswordHistoryBuilder result(String result) {
            this.result = result;
            return this;
        }

        public MemberPasswordHistoryBuilder message(String message) {
            this.message = message;
            return this;
        }

        public MemberPasswordHistoryBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public MemberPasswordHistoryBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public MemberPasswordHistoryBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MemberPasswordHistory build() {
            MemberPasswordHistory history = new MemberPasswordHistory(this.member, this.passwordHash);
            history.id = this.id;
            history.createdAt = this.createdAt;
            history.action = this.action;
            history.result = this.result;
            history.message = this.message;
            history.ipAddress = this.ipAddress;
            history.userAgent = this.userAgent;
            history.timestamp = this.timestamp;
            return history;
        }
    }
}