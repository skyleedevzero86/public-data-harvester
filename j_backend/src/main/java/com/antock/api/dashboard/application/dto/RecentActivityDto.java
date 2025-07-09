package com.antock.api.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecentActivityDto {
    private String message;
    private String timeAgo;
    private String type;
    private String icon;
}