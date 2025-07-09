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

    public String getSiNmForSearch() {
        return (siNm != null && !siNm.trim().isEmpty()) ? siNm.trim() : null;
    }

    public String getSggNmForSearch() {
        return (sggNm != null && !sggNm.trim().isEmpty()) ? sggNm.trim() : null;
    }
}