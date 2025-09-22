package com.antock.api.member.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public static MemberUpdateRequestBuilder builder() {
        return new MemberUpdateRequestBuilder();
    }

    public static class MemberUpdateRequestBuilder {
        private String nickname;
        private String email;

        public MemberUpdateRequestBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public MemberUpdateRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public MemberUpdateRequest build() {
            MemberUpdateRequest request = new MemberUpdateRequest();
            request.nickname = this.nickname;
            request.email = this.email;
            return request;
        }
    }
}