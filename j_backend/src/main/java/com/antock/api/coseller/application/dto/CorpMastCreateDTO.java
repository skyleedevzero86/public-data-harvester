package com.antock.api.coseller.application.dto;

import com.antock.api.coseller.domain.CorpMast;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CorpMastCreateDTO {

    private String sellerId;
    private String bizNm;
    private String bizNo;
    private String corpRegNo;
    private String regionCd;
    private String siNm;
    private String sggNm;
    private String username;

    public CorpMast toEntity() {
        return CorpMast.builder()
                .sellerId(this.sellerId != null ? this.sellerId : "")
                .bizNm(this.bizNm != null ? this.bizNm : "")
                .bizNo(this.bizNo != null ? this.bizNo : "")
                .corpRegNo(this.corpRegNo != null ? this.corpRegNo : "")
                .regionCd(this.regionCd != null ? this.regionCd : "")
                .siNm(this.siNm != null ? this.siNm : "")
                .sggNm(this.sggNm != null ? this.sggNm : "")
                .username(this.username != null ? this.username : "")
                .build();
    }
}