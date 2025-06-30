package com.antock.api.coseller.application.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * 행정코드 받아오기 api에서 json "result":"juso"파싱
 */
@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionJuso {
    private String admCd; //행정코드
}