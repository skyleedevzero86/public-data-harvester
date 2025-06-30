package com.antock.api.coseller.application.dto;

import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.global.common.valid.ValidEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class RegionRequestDto {

    @ValidEnum(target = City.class,message= "시/도 정보를 입력해 주세요.")
    private City city;
    @ValidEnum(target = District.class,message= "구/군 정보를 입력해 주세요.")
    private District district;
}