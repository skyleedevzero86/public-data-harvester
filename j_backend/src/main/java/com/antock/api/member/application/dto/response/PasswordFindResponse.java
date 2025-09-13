package com.antock.api.member.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 찾기 응답 DTO", example = """
    {
      "success": true,
      "message": "비밀번호 재설정 링크가 이메일로 전송되었습니다.",
      "email": "test@example.com",
      "expiresAt": "2024-01-15T11:30:00",
      "expiryMinutes": 30,
      "resetLink": "http://localhost:8080/members/password/reset?token=abc123"
    }
    """)
public class PasswordFindResponse {

  @Schema(description = "성공 여부", example = "true")
  private boolean success;

  @Schema(description = "응답 메시지", example = "비밀번호 재설정 링크가 이메일로 전송되었습니다.")
  private String message;

  @Schema(description = "이메일 주소", example = "test@example.com")
  private String email;

  @Schema(description = "토큰 만료 시간", example = "2024-01-15T11:30:00")
  private String expiresAt;

  @Schema(description = "만료 시간(분)", example = "30")
  private int expiryMinutes;

  @Schema(description = "재설정 링크", example = "http://localhost:8080/members/password/reset?token=abc123")
  private String resetLink;
}