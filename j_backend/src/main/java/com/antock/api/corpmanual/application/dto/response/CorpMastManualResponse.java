package com.antock.api.corpmanual.application.dto.response;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.global.utils.NumberFormatUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "법인 정보 응답 DTO", example = """
        {
          "id": 1,
          "sellerId": "seller123",
          "bizNm": "삼성전자주식회사",
          "bizNo": "124-81-00998",
          "corpRegNo": "110111-1234567",
          "regionCd": "11680",
          "siNm": "서울특별시",
          "sggNm": "강남구",
          "username": "admin",
          "description": "전자제품 제조업체",
          "formattedBizNo": "124-81-00998",
          "fullAddress": "서울특별시 강남구",
          "createDate": "2024-01-15T10:30:00",
          "modifyDate": "2024-01-15T10:30:00"
        }
        """)
public class CorpMastManualResponse {

    @Schema(description = "법인 고유 ID", example = "1", required = true)
    private Long id;

    @Schema(description = "판매자 ID", example = "seller123", maxLength = 100, required = true)
    private String sellerId;

    @Schema(description = "법인명 (사업체명)", example = "삼성전자주식회사", maxLength = 200, required = true)
    private String bizNm;

    @Schema(description = "사업자번호 (원본 형태)", example = "1248100998", maxLength = 20, required = true)
    private String bizNo;

    @Schema(description = "법인등록번호", example = "110111-1234567", maxLength = 20, required = true)
    private String corpRegNo;

    @Schema(description = "지역코드 (행정구역코드)", example = "11680", maxLength = 20, required = true)
    private String regionCd;

    @Schema(description = "시/도명", example = "서울특별시", maxLength = 50, required = true)
    private String siNm;

    @Schema(description = "구/군명", example = "강남구", maxLength = 50, required = true)
    private String sggNm;

    @Schema(description = "등록자 사용자명", example = "admin", maxLength = 100, required = true)
    private String username;

    @Schema(description = "법인 상세 설명", example = "전자제품 제조업체", maxLength = 2000, nullable = true)
    private String description;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createDate;

    @Schema(description = "수정일시", example = "2024-01-15T10:30:00", type = "string", format = "date-time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modifyDate;

    @Schema(description = "포맷된 사업자번호 (하이픈 포함)", example = "124-81-00998", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("formattedBizNo")
    public String getFormattedBizNo() {
        if (bizNo == null || bizNo.length() != 10)
            return bizNo;
        return bizNo.substring(0, 3) + "-" + bizNo.substring(3, 5) + "-" + bizNo.substring(5);
    }

    @Schema(description = "전체 주소 (시/도 + 구/군)", example = "서울특별시 강남구", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("fullAddress")
    public String getFullAddress() {
        return (siNm != null ? siNm : "") + " " + (sggNm != null ? sggNm : "");
    }

    @Schema(description = "포맷된 ID (콤마 포함)", example = "1,234", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("formattedId")
    public String getFormattedId() {
        return NumberFormatUtil.formatLong(id);
    }

    @Schema(description = "포맷된 지역코드 (콤마 포함)", example = "11,680", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty("formattedRegionCd")
    public String getFormattedRegionCd() {
        return NumberFormatUtil.formatStringNumber(regionCd);
    }

    public static CorpMastManualResponse from(CorpMast entity) {
        return CorpMastManualResponse.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .bizNm(entity.getBizNm())
                .bizNo(entity.getBizNo())
                .corpRegNo(entity.getCorpRegNo())
                .regionCd(entity.getRegionCd())
                .siNm(entity.getSiNm())
                .sggNm(entity.getSggNm())
                .username(entity.getUsername())
                .description(entity.getDescription())
                .createDate(entity.getCreateDate())
                .modifyDate(entity.getModifyDate())
                .build();
    }
}