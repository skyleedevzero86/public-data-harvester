package com.antock.api.corpmanual.application.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorpMastManualRequest {

    private String bizNm;       // 법인명
    private String bizNo;       // 사업자번호
    private String sellerId;    // 판매자ID
    private String corpRegNo;   // 법인등록번호
    private String city;        // 시/도 (siNm)
    private String district;    // 구/군 (sggNm)

    private int page = 0;
    private int size = 20;
    private String sort = "id,desc";

    public String getBizNmForSearch() { return bizNm != null ? bizNm.trim() : null; }
    public String getBizNoForSearch() { return bizNo != null ? bizNo.trim().replaceAll("-", "") : null; }
    public String getSellerIdForSearch() { return sellerId != null ? sellerId.trim() : null; }
    public String getCorpRegNoForSearch() { return corpRegNo != null ? corpRegNo.trim() : null; }
    public String getCityForSearch() { return (city != null && !city.trim().isEmpty()) ? city.trim() : null; }
    public String getDistrictForSearch() { return (district != null && !district.trim().isEmpty()) ? district.trim() : null; }
    public boolean hasSearchCondition() {
        return (bizNm != null && !bizNm.trim().isEmpty()) ||
                (bizNo != null && !bizNo.trim().isEmpty()) ||
                (sellerId != null && !sellerId.trim().isEmpty()) ||
                (corpRegNo != null && !corpRegNo.trim().isEmpty()) ||
                (city != null && !city.trim().isEmpty()) ||
                (district != null && !district.trim().isEmpty());
    }
}