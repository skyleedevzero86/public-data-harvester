package com.antock.api.coseller.application.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 행정코드 받아오기 api에서 json "result": "common"파싱
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionCommon {
    private String errorCode;
    private String errorMessage;
}