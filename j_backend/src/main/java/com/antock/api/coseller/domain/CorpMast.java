package com.antock.api.coseller.domain;

import com.antock.global.common.entity.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "corp_mast",
        uniqueConstraints = {
                @UniqueConstraint(name = "CORP_MAST_UNIQUE", columnNames = { "biz_no" })
        },
        indexes = {
                @Index(name = "idx_corp_mast_biz_no", columnList = "biz_no"),
                @Index(name = "idx_corp_mast_corp_reg_no", columnList = "corp_reg_no"),
                @Index(name = "idx_corp_mast_seller_id", columnList = "seller_id"),
                @Index(name = "idx_corp_mast_username", columnList = "username"),
                @Index(name = "idx_corp_mast_location", columnList = "si_nm, sgg_nm"),
                @Index(name = "idx_corp_mast_region_cd", columnList = "region_cd")
        })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"histories"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CorpMast extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String sellerId;

    @Column(nullable = false, length = 200)
    private String bizNm;

    @Column(nullable = false, unique = true, length = 20)
    private String bizNo;

    @Column(nullable = false, length = 20)
    private String corpRegNo;

    @Column(nullable = false, length = 20)
    private String regionCd;

    @Column(nullable = false, length = 50)
    private String siNm;

    @Column(nullable = false, length = 50)
    private String sggNm;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "corpMast", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    private List<CorpMastHistory> histories = new ArrayList<>();

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId != null ? sellerId.trim() : "";
    }

    public void setBizNm(String bizNm) {
        this.bizNm = bizNm != null ? bizNm.trim() : "";
    }

    public void setCorpRegNo(String corpRegNo) {
        this.corpRegNo = corpRegNo != null ? corpRegNo.trim() : "";
    }

    public void setRegionCd(String regionCd) {
        this.regionCd = regionCd != null ? regionCd.trim() : "";
    }

    public void setSiNm(String siNm) {
        this.siNm = siNm != null ? siNm.trim() : "";
    }

    public void setSggNm(String sggNm) {
        this.sggNm = sggNm != null ? sggNm.trim() : "";
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo != null ? bizNo.trim() : "";
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public void addHistory(CorpMastHistory history) {
        this.histories.add(history);
        history.setCorpMast(this);
    }

    public String getFullAddress() {
        return String.format("%s %s", siNm, sggNm);
    }

    public String getFormattedBizNo() {
        if (bizNo == null || bizNo.length() != 10) return bizNo;
        return String.format("%s-%s-%s",
                bizNo.substring(0, 3),
                bizNo.substring(3, 5),
                bizNo.substring(5));
    }
}