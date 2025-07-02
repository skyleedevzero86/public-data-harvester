package com.antock.api.member.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberLoginResponse {

    private MemberResponse member;
    private String accessToken;
    private String refreshToken;
    private String apiKey;
}