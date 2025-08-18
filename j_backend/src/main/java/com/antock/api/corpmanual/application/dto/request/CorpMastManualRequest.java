package com.antock.api.corpmanual.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "법인 정보 검색 요청")
public class CorpMastManualRequest {

    @Schema(description = "법인명", example = "삼성전자")
    private String bizNm;

    @Schema(description = "사업자번호", example = "124-81-00998")
    private String bizNo;

    @Schema(description = "판매자ID", example = "seller123")
    private String sellerId;

    @Schema(description = "법인등록번호", example = "110111-1234567")
    private String corpRegNo;

    @Schema(description = "시/도", example = "서울특별시")
    private String city;

    @Schema(description = "구/군", example = "강남구")
    private String district;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20")
    private int size = 20;

    @Schema(description = "정렬 조건 (예: id,desc)", example = "id,desc")
    private String sort = "id,desc";

    public String getBizNmForSearch() {
        return bizNm != null ? bizNm.trim() : null;
    }

    public String getBizNoForSearch() {
        return bizNo != null ? bizNo.trim().replaceAll("-", "") : null;
    }

    public String getSellerIdForSearch() {
        return sellerId != null ? sellerId.trim() : null;
    }

    public String getCorpRegNoForSearch() {
        return corpRegNo != null ? corpRegNo.trim() : null;
    }

    public String getCityForSearch() {
        return (city != null && !city.trim().isEmpty()) ? city.trim() : null;
    }

    public String getDistrictForSearch() {
        return (district != null && !district.trim().isEmpty()) ? district.trim() : null;
    }

    public boolean hasSearchCondition() {
        return getBizNmForSearch() != null || getBizNoForSearch() != null ||
                getSellerIdForSearch() != null || getCorpRegNoForSearch() != null ||
                getCityForSearch() != null || getDistrictForSearch() != null;
    }
}