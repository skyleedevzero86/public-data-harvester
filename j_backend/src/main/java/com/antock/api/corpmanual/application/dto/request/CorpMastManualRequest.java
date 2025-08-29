package com.antock.api.corpmanual.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "법인 정보 검색 요청 DTO", example = """
        {
          "bizNm": "삼성전자",
          "bizNo": "124-81-00998",
          "sellerId": "seller123",
          "corpRegNo": "110111-1234567",
          "city": "서울특별시",
          "district": "강남구",
          "page": 0,
          "size": 20,
          "sort": "id,desc"
        }
        """)
public class CorpMastManualRequest {

    @Schema(description = "법인명 (부분 일치 검색, 대소문자 무관)", example = "삼성전자", maxLength = 200, nullable = true)
    @Size(max = 200, message = "법인명은 200자를 초과할 수 없습니다")
    private String bizNm;

    @Schema(description = "사업자번호 (하이픈 포함/제외 모두 가능, 정확 일치)", example = "124-81-00998", pattern = "^\\d{3}-?\\d{2}-?\\d{5}$", nullable = true)
    @Pattern(regexp = "^\\d{3}-?\\d{2}-?\\d{5}$", message = "올바른 사업자번호 형식이 아닙니다 (예: 124-81-00998 또는 1248100998)")
    private String bizNo;

    @Schema(description = "판매자 ID (부분 일치 검색, 대소문자 무관)", example = "seller123", maxLength = 100, nullable = true)
    @Size(max = 100, message = "판매자 ID는 100자를 초과할 수 없습니다")
    private String sellerId;

    @Schema(description = "법인등록번호 (정확 일치)", example = "110111-1234567", pattern = "^\\d{6}-?\\d{7}$", nullable = true)
    @Pattern(regexp = "^\\d{6}-?\\d{7}$", message = "올바른 법인등록번호 형식이 아닙니다 (예: 110111-1234567 또는 1101111234567)")
    private String corpRegNo;

    @Schema(description = "시/도 (정확 일치)", example = "서울특별시", allowableValues = {
            "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시",
            "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도",
            "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"
    }, nullable = true)
    @Size(max = 50, message = "시/도명은 50자를 초과할 수 없습니다")
    private String city;

    @Schema(description = "구/군 (정확 일치, 선택한 시/도에 속한 구/군만 유효)", example = "강남구", maxLength = 50, nullable = true)
    @Size(max = 50, message = "구/군명은 50자를 초과할 수 없습니다")
    private String district;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private int page = 0;

    @Schema(description = "페이지 크기 (최대 100개)", example = "20", minimum = "1", maximum = "100", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100을 초과할 수 없습니다")
    private int size = 20;

    @Schema(description = "정렬 조건 (필드명,정렬방향 형식)", example = "id,desc", allowableValues = {
            "id,asc", "id,desc",
            "bizNm,asc", "bizNm,desc",
            "bizNo,asc", "bizNo,desc",
            "sellerId,asc", "sellerId,desc",
            "createDate,asc", "createDate,desc",
            "modifyDate,asc", "modifyDate,desc"
    }, defaultValue = "id,desc")
    @Pattern(regexp = "^(id|bizNm|bizNo|sellerId|createDate|modifyDate),(asc|desc)$", message = "정렬 조건은 '필드명,정렬방향' 형식이어야 합니다 (예: id,desc)")
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