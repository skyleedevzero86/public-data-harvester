package com.antock.api.coseller.application.dto.api;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * csv 파일에서 읽어오는 통신 판매 정보
 */
@Getter
@Builder
@ToString
public class BizCsvInfoDto {
    private String sellerId; //통신판매번호
    private String bizNm; //상호
    private String bizNo; //사업자 등록번호
    private String bizType; //법인 여부
    private String bizAddress; //사업자 소재지
    private String bizNesAddress; //사업자 소재지 도로명주소
}