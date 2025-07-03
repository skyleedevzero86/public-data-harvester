package com.antock.api.member.application.dto.response;

import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private MemberStatus status;
    private Role role;
    private Date createDate;
    private LocalDateTime modifyDate;
    private Date lastLoginAt;
    private Date approvedAt;
    private String apiKey;

    public static MemberResponse from(Member member) {
        Date createDate = member.getCreateDate() != null
                ? Date.from(member.getCreateDate().atZone(ZoneId.of("Asia/Seoul")).toInstant())
                : null;
        Date lastLoginAtDate = member.getLastLoginAt() != null
                ? Date.from(member.getLastLoginAt().atZone(ZoneId.of("Asia/Seoul")).toInstant())
                : null;
        Date approvedAt = member.getApprovedAt() != null
                ? Date.from(member.getApprovedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant())
                : null;

        return MemberResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .status(member.getStatus())
                .role(member.getRole())
                .createDate(createDate)
                .modifyDate(member.getModifyDate())
                .lastLoginAt(lastLoginAtDate)
                .approvedAt(approvedAt)
                .apiKey(member.getApiKey())
                .build();
    }
}