package com.antock.api.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    private String message;
    private String timeAgo;
    private String type;
    private String icon;
}