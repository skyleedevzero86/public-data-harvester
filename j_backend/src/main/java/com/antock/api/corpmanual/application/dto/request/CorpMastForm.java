package com.antock.api.corpmanual.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorpMastForm {
    private Long id;
    private String sellerId;
    private String bizNm;
    private String bizNo;
    private String corpRegNo;
    private String regionCd;
    private String siNm;
    private String sggNm;
    private String description;
    private String username;
}