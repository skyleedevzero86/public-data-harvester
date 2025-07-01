package com.antock.api.coseller.application.dto;

import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.global.common.valid.ValidEnum;
import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegionRequestDto {

    @ValidEnum(target = City.class, message = "유효한 시/도 정보를 입력해 주세요: {0}")
    private City city;

    @ValidEnum(target = District.class, message = "구/군 정보는 강남구, 강동구, 강북구, 강서구만 확인할 수 있습니다: {0}")
    private District district;
}