package com.antock.api.corpsearch.application.dto.response;

import com.antock.api.coseller.domain.CorpMast;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CorpMastSearchResponse {

    private Long id;
    private String sellerId;
    private String bizNm;
    private String bizNo;
    private String corpRegNo;
    private String regionCd;
    private String siNm;
    private String sggNm;
    private String username;

    public String getFormattedBizNo() {
        if (bizNo == null || bizNo.length() != 10) {
            return bizNo;
        }
        return bizNo.substring(0, 3) + "-" + bizNo.substring(3, 5) + "-" + bizNo.substring(5);
    }

    public String getFullAddress() {
        return siNm + " " + sggNm;
    }

    public static CorpMastSearchResponse from(CorpMast entity) {
        return CorpMastSearchResponse.builder()
                .id(entity.getId())
                .sellerId(entity.getSellerId())
                .bizNm(entity.getBizNm())
                .bizNo(entity.getBizNo())
                .corpRegNo(entity.getCorpRegNo())
                .regionCd(entity.getRegionCd())
                .siNm(entity.getSiNm())
                .sggNm(entity.getSggNm())
                .username(entity.getUsername())
                .build();
    }
}