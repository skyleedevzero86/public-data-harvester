package com.antock.api.coseller.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum District {
    강남구("강남구"), 강동구("강동구"), 강북구("강북구"), 강서구("강서구"),
    관악구("관악구"), 광진구("광진구"), 구로구("구로구"), 금천구("금천구"),
    노원구("노원구"), 도봉구("도봉구"), 동대문구("동대문구"), 동작구("동작구"),
    마포구("마포구"), 서대문구("서대문구"), 서초구("서초구"), 성동구("성동구"),
    성북구("성북구"), 송파구("송파구"), 양천구("양천구"), 영등포구("영등포구"),
    용산구("용산구"), 은평구("은평구"), 종로구("종로구"), 중구("중구"), 중랑구("중랑구"),
    제주시("제주시"), 서귀포시("서귀포시"),
    // ... (각 시/도별 실제 구/군 추가)
    ;

    private final String value;

    District(String value) { this.value = value; }

    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static District fromValue(String value) {
        for (District district : District.values()) {
            if (district.value.equalsIgnoreCase(value)) {
                return district;
            }
        }
        throw new IllegalArgumentException("Unknown district: " + value);
    }
}