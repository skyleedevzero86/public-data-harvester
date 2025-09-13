package com.antock.api.member.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 요청 DTO", example = """
        {
          "token": "reset-token-12345",
          "newPassword": "NewPassword123!",
          "newPasswordConfirm": "NewPassword123!"
        }
        """)
public class PasswordResetRequest {

    @Schema(description = "재설정 토큰", example = "reset-token-12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "재설정 토큰은 필수입니다.")
    private String token;

    @Schema(description = "새 비밀번호", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>_+=\\-\\[\\]\\\\;'`~])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>_+=\\-\\[\\]\\\\;'`~]{8,}$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;

    @Schema(description = "새 비밀번호 확인", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;

    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}