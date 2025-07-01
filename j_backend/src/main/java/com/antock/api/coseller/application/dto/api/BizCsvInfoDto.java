package com.antock.api.coseller.application.dto.api;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class BizCsvInfoDto {
    private String sellerId;
    private String bizNm;
    private String bizNo;
    private String bizType;
    private String bizAddress;
    private String bizNesAddress;
}