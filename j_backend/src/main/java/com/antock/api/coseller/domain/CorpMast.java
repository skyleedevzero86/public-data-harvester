package com.antock.api.coseller.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "corp_mast", uniqueConstraints = {
        @UniqueConstraint(name = "CORP_MAST_UNIQUE", columnNames = { "biz_no" })
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

    @Column(nullable = false)
    private String siNm;

    @Column(nullable = false)
    private String sggNm;

    @Column(nullable = false)
    private String username;

    @Column(nullable = true, length = 2000)
    private String description;

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

    public void setSiNm(String siNm) {
        this.siNm = siNm;
    }

    public void setSggNm(String sggNm) {
        this.sggNm = sggNm;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

