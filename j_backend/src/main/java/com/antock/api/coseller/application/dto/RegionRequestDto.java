package com.antock.api.coseller.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RegionRequestDto {

    @NotBlank(message= "시/도 정보를 입력해 주세요.")
    private String city;
    @NotBlank(message= "구/군 정보를 입력해 주세요.")
    private String district;
}