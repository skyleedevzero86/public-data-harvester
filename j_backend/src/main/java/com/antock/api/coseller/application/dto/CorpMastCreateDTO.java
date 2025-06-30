package com.antock.api.coseller.application.dto;

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

    public CorpMast toEntity(CorpMastCreateDTO createDto) {
        return CorpMast.builder()
                .sellerId(createDto.sellerId)
                .bizNm(createDto.bizNm)
                .bizNo(createDto.bizNo)
                .corpRegNo(createDto.corpRegNo)
                .regionCd(createDto.regionCd)
                .build();
    }

}