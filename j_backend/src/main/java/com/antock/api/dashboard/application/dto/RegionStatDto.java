package com.antock.api.dashboard.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "지역별 통계 데이터 DTO", example = """
        {
          "city": "서울특별시",
          "district": "강남구",
          "totalCount": 15423,
          "validCorpRegNoCount": 14876,
          "validRegionCdCount": 15201,
          "completionRate": 97.5
        }
        """)
public class RegionStatDto {

    @Schema(description = "시/도명", example = "서울특별시", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    @Schema(description = "구/군명", example = "강남구", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String district;

    @Schema(description = "해당 지역의 총 법인 수", example = "15423", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private long totalCount;

    @Schema(description = "유효한 법인등록번호를 가진 법인 수", example = "14876", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private long validCorpRegNoCount;

    @Schema(description = "유효한 지역코드를 가진 법인 수", example = "15201", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private long validRegionCdCount;

    @Schema(description = "데이터 완성도 (유효한 법인등록번호 + 지역코드 비율)", example = "97.5", minimum = "0.0", maximum = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private double completionRate;

    public RegionStatDto(String city, String district, Long totalCount, Long validCorpRegNoCount,
            Long validRegionCdCount) {
        this.city = city;
        this.district = district;
        this.totalCount = totalCount != null ? totalCount : 0L;
        this.validCorpRegNoCount = validCorpRegNoCount != null ? validCorpRegNoCount : 0L;
        this.validRegionCdCount = validRegionCdCount != null ? validRegionCdCount : 0L;
        this.completionRate = this.totalCount > 0
                ? (double) (this.validCorpRegNoCount + this.validRegionCdCount) / (this.totalCount * 2) * 100
                : 0.0;
    }
}