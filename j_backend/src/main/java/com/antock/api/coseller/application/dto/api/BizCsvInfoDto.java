package com.antock.api.coseller.application.dto.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BizCsvInfoDto {
    private String sellerId;
    private String city;
    private String bizNm;
    private String bizNo;
    private String bizType;
    private String ownerName;
    private String phone;
    private String email;
    private String date;
    private String address;
    private String bizAddress;
    private String bizNesAddress;
}