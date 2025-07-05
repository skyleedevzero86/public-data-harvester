package com.antock.api.member.application.dto.response;

import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponse {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String apiKey;
    private MemberStatus status;
    private Role role;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private LocalDateTime lastLoginAt;
    private Integer loginFailCount;
    private LocalDateTime accountLockedAt;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime passwordChangedAt;
    private Integer passwordChangeCount;
    private LocalDateTime lastPasswordChangeDate;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .apiKey(member.getApiKey())
                .status(member.getStatus())
                .role(member.getRole())
                .createDate(member.getCreateDate())
                .modifyDate(member.getModifyDate())
                .lastLoginAt(member.getLastLoginAt())
                .loginFailCount(member.getLoginFailCount())
                .accountLockedAt(member.getAccountLockedAt())
                .approvedBy(member.getApprovedBy())
                .approvedAt(member.getApprovedAt())
                .passwordChangedAt(member.getPasswordChangedAt())
                .passwordChangeCount(member.getPasswordChangeCount())
                .lastPasswordChangeDate(member.getLastPasswordChangeDate() != null ?
                        member.getLastPasswordChangeDate().atStartOfDay() : null)
                .build();
    }
}