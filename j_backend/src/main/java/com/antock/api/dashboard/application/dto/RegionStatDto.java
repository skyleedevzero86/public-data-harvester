package com.antock.api.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegionStatDto {
    private String city;
    private String district;
    private long totalCount;
    private long validCorpRegNoCount;
    private long validRegionCdCount;
    private double completionRate;
}