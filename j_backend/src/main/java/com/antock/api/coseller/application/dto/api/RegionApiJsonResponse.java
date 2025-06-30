package com.antock.api.coseller.application.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionApiJsonResponse {
    private RegionResult results;
}