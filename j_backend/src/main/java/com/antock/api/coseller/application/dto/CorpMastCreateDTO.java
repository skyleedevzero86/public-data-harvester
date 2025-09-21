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
    private String repNm; // 대표자명
    private String estbDt; // 설립일자
    private String roadNmAddr; // 도로명주소
    private String jibunAddr; // 지번주소
    private String corpStatus; // 법인상태

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
                .repNm(this.repNm != null ? this.repNm : "")
                .estbDt(this.estbDt != null ? this.estbDt : "")
                .roadNmAddr(this.roadNmAddr != null ? this.roadNmAddr : "")
                .jibunAddr(this.jibunAddr != null ? this.jibunAddr : "")
                .corpStatus(this.corpStatus != null ? this.corpStatus : "")
                .build();
    }
}