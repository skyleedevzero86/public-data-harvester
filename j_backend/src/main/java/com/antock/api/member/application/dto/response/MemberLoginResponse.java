package com.antock.api.member.application.dto.response;

public class MemberLoginResponse {

    private MemberResponse member;
    private String accessToken;
    private String refreshToken;
    private String apiKey;

    public MemberLoginResponse() {}

    public MemberResponse getMember() {
        return member;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public static MemberLoginResponseBuilder builder() {
        return new MemberLoginResponseBuilder();
    }

    public static class MemberLoginResponseBuilder {
        private MemberResponse member;
        private String accessToken;
        private String refreshToken;
        private String apiKey;

        public MemberLoginResponseBuilder member(MemberResponse member) {
            this.member = member;
            return this;
        }

        public MemberLoginResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public MemberLoginResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public MemberLoginResponseBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public MemberLoginResponse build() {
            MemberLoginResponse response = new MemberLoginResponse();
            response.member = this.member;
            response.accessToken = this.accessToken;
            response.refreshToken = this.refreshToken;
            response.apiKey = this.apiKey;
            return response;
        }
    }
}