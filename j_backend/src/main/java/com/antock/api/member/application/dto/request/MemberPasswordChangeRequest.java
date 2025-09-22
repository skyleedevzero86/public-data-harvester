package com.antock.api.member.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberPasswordChangeRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 8, max = 20)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>_+=\\-\\[\\]\\\\;'`~])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>_+=\\-\\[\\]\\\\;'`~]{8,}$")
    private String newPassword;

    @NotBlank
    private String newPasswordConfirm;

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public static MemberPasswordChangeRequestBuilder builder() {
        return new MemberPasswordChangeRequestBuilder();
    }

    public static class MemberPasswordChangeRequestBuilder {
        private String oldPassword;
        private String newPassword;
        private String newPasswordConfirm;

        public MemberPasswordChangeRequestBuilder oldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
            return this;
        }

        public MemberPasswordChangeRequestBuilder newPassword(String newPassword) {
            this.newPassword = newPassword;
            return this;
        }

        public MemberPasswordChangeRequestBuilder newPasswordConfirm(String newPasswordConfirm) {
            this.newPasswordConfirm = newPasswordConfirm;
            return this;
        }

        public MemberPasswordChangeRequest build() {
            MemberPasswordChangeRequest request = new MemberPasswordChangeRequest();
            request.oldPassword = this.oldPassword;
            request.newPassword = this.newPassword;
            request.newPasswordConfirm = this.newPasswordConfirm;
            return request;
        }
    }
}