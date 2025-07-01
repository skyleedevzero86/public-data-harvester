package com.antock.api.coseller.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "corp_mast", uniqueConstraints = {
        @UniqueConstraint(
                name = "CORP_MAST_UNIQUE",
                columnNames = {"biz_no"}
        )
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CorpMast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String bizNm;

    @Column(nullable = false, unique = true)
    private String bizNo;

    @Column(nullable = false)
    private String corpRegNo;

    @Column(nullable = false)
    private String regionCd;

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setBizNm(String bizNm) {
        this.bizNm = bizNm;
    }

    public void setCorpRegNo(String corpRegNo) {
        this.corpRegNo = corpRegNo;
    }

    public void setRegionCd(String regionCd) {
        this.regionCd = regionCd;
    }
}