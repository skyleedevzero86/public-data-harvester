package com.antock.api.coseller.application.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionJuso {
    private String admCd;
    private String siNm;
    private String sggNm;
    private String rnMgtSn;
    private String corpRegNo;
}