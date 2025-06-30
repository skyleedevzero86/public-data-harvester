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

    public CorpMast toEntity() {
        return CorpMast.builder()
                .sellerId(this.sellerId)
                .bizNm(this.bizNm)
                .bizNo(this.bizNo)
                .corpRegNo(this.corpRegNo)
                .regionCd(this.regionCd)
                .build();
    }

}