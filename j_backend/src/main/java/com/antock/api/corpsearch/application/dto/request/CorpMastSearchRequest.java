package com.antock.api.corpsearch.application.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CorpMastSearchRequest {

    private String bizNm;       // 법인명
    private String bizNo;       // 사업자번호
    private String sellerId;    // 판매자ID
    private String corpRegNo;   // 법인등록번호
    private String city;        // 시/도 (siNm)
    private String district;    // 구/군 (sggNm)

    private int page = 0;
    private int size = 20;
    private String sort = "id,desc";

    public String getBizNmForSearch() {
        return isNotEmpty(bizNm) ? bizNm.trim() : null;
    }

    public String getBizNoForSearch() {
        return isNotEmpty(bizNo) ? bizNo.trim().replaceAll("-", "") : null;
    }

    public String getSellerIdForSearch() {
        return isNotEmpty(sellerId) ? sellerId.trim() : null;
    }

    public String getCorpRegNoForSearch() {
        return isNotEmpty(corpRegNo) ? corpRegNo.trim() : null;
    }

    public String getCityForSearch() {
        return isNotEmpty(city) ? city.trim() : null;
    }

    public String getDistrictForSearch() {
        return isNotEmpty(district) ? district.trim() : null;
    }

    public boolean hasSearchCondition() {
        return isNotEmpty(bizNm) ||
                isNotEmpty(bizNo) ||
                isNotEmpty(sellerId) ||
                isNotEmpty(corpRegNo) ||
                isNotEmpty(city) ||
                isNotEmpty(district);
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}