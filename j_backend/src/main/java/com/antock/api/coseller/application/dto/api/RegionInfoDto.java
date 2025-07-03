package com.antock.api.coseller.application.dto.api;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionInfoDto {
    private String regionCd;
    private String siNm;
    private String sggNm;
    private String corpRegNo;
    private String rnMgtSn;
}