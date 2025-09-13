package com.antock.api.dashboard.application.dto;

import com.antock.global.utils.NumberFormatUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
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
                  "validCorpRegNoCount": 15000,
                  "validRegionCdCount": 15400,
                  "formattedCount": "15,423",
                  "completionRate": 76.95
                }
                """)
public class RegionStatDto {

        @Schema(description = "시/도명", example = "서울특별시", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        private String city;

        @Schema(description = "구/군명", example = "강남구", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        private String district;

        @Schema(description = "해당 지역의 총 법인 수", example = "15423", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        private long totalCount;

        @Schema(description = "유효한 법인등록번호를 가진 법인 수", example = "15000", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        private long validCorpRegNoCount;

        @Schema(description = "유효한 지역코드를 가진 법인 수", example = "15400", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        private long validRegionCdCount;

        @JsonProperty("formattedCount")
        @Schema(description = "포맷된 법인 수 (천 단위 구분자 포함)", example = "15,423", requiredMode = Schema.RequiredMode.REQUIRED)
        private String formattedCount;

        @Schema(description = "데이터 완성도 (백분율)", example = "76.95", minimum = "0.0", maximum = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
        private double completionRate;

        public RegionStatDto(String city, String district, long totalCount, long validCorpRegNoCount,
                        long validRegionCdCount) {
                this.city = city;
                this.district = district;
                this.totalCount = totalCount;
                this.validCorpRegNoCount = validCorpRegNoCount;
                this.validRegionCdCount = validRegionCdCount;
                this.completionRate = calculateCompletionRate();
        }

        public String getFormattedCount() {
                if (formattedCount == null) {
                        formattedCount = NumberFormatUtil.formatNumber(totalCount);
                }
                return formattedCount;
        }

        private double calculateCompletionRate() {
                if (totalCount == 0) {
                        return 0.0;
                }
                double corpRegNoRate = (double) validCorpRegNoCount / totalCount * 100;
                double regionCdRate = (double) validRegionCdCount / totalCount * 100;

                return Math.round((corpRegNoRate + regionCdRate) / 2 * 100.0) / 100.0;
        }

        @Override
        public String toString() {
                return "RegionStatDto{" +
                                "city='" + city + '\'' +
                                ", district='" + district + '\'' +
                                ", totalCount=" + totalCount +
                                ", validCorpRegNoCount=" + validCorpRegNoCount +
                                ", validRegionCdCount=" + validRegionCdCount +
                                ", formattedCount='" + getFormattedCount() + '\'' +
                                ", completionRate=" + completionRate +
                                '}';
        }
}