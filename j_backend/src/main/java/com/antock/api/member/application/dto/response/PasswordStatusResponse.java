package com.antock.api.member.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordStatusResponse {
    private boolean isPasswordChangeRequired;
    private boolean isPasswordChangeRecommended;
    private long todayPasswordChangeCount;
    private int maxDailyPasswordChanges;
}