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

    public RegionStatDto(String city, String district, Long totalCount, Long validCorpRegNoCount, Long validRegionCdCount) {
        this.city = city;
        this.district = district;
        this.totalCount = totalCount != null ? totalCount : 0L;
        this.validCorpRegNoCount = validCorpRegNoCount != null ? validCorpRegNoCount : 0L;
        this.validRegionCdCount = validRegionCdCount != null ? validRegionCdCount : 0L;
        this.completionRate = this.totalCount > 0 ?
                (double) (this.validCorpRegNoCount + this.validRegionCdCount) / (this.totalCount * 2) * 100 : 0.0;
    }
}