package com.antock.api.member.application.dto.response;

import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "회원 정보 응답 DTO", example = """
        {
          "id": 1,
          "username": "testuser",
          "nickname": "테스트사용자",
          "email": "test@example.com",
          "apiKey": "generated-api-key-12345",
          "status": "APPROVED",
          "role": "USER",
          "createDate": "2024-01-15T10:30:00",
          "modifyDate": "2024-01-15T10:30:00",
          "lastLoginAt": "2024-01-15T10:30:00",
          "loginFailCount": 0,
          "accountLockedAt": null,
          "approvedBy": 2,
          "approvedAt": "2024-01-15T11:00:00",
          "passwordChangedAt": "2024-01-15T10:30:00",
          "passwordChangeCount": 1,
          "lastPasswordChangeDate": "2024-01-15T10:30:00"
        }
        """)
public class MemberResponse {

    @Schema(description = "회원 고유 ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "사용자명 (로그인 ID)", example = "testuser", maxLength = 50)
    private String username;

    @Schema(description = "닉네임 (화면 표시용)", example = "테스트사용자", maxLength = 50)
    private String nickname;

    @Schema(description = "이메일 주소", example = "test@example.com", maxLength = 100, format = "email")
    private String email;

    @Schema(description = "API 키 (외부 API 호출용)", example = "generated-api-key-12345", maxLength = 64, accessMode = Schema.AccessMode.READ_ONLY)
    private String apiKey;

    @Schema(description = "회원 상태", example = "APPROVED", implementation = MemberStatus.class)
    private MemberStatus status;

    @Schema(description = "회원 역할", example = "USER", implementation = Role.class)
    private Role role;

    @Schema(description = "계정 생성일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createDate;

    @Schema(description = "계정 수정일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifyDate;

    @Schema(description = "마지막 로그인 일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    @Schema(description = "로그인 실패 횟수", example = "0", minimum = "0", maximum = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer loginFailCount;

    @Schema(description = "계정 잠금 일시", example = "null", type = "string", format = "date-time", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime accountLockedAt;

    @Schema(description = "승인한 관리자 ID", example = "2", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    private Long approvedBy;

    @Schema(description = "승인 일시", example = "2024-01-15T11:00:00", type = "string", format = "date-time", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;

    @Schema(description = "비밀번호 변경 일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime passwordChangedAt;

    @Schema(description = "비밀번호 변경 횟수", example = "1", minimum = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer passwordChangeCount;

    @Schema(description = "마지막 비밀번호 변경 날짜", example = "2024-01-15T10:30:00", type = "string", format = "date-time", nullable = true, accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
                .lastPasswordChangeDate(
                        member.getLastPasswordChangeDate() != null ? member.getLastPasswordChangeDate().atStartOfDay()
                                : null)
                .build();
    }
}