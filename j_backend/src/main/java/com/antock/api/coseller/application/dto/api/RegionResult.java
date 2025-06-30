package com.antock.api.coseller.application.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * 행정코드 받아오기 api에서 json "result" 파싱
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionResult {

    private RegionCommon common;
    private List<RegionJuso> juso;
}