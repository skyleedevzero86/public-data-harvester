package com.antock.api.coseller.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum City {
    서울특별시("서울특별시"),
    부산광역시("부산광역시"),
    대구광역시("대구광역시"),
    인천광역시("인천광역시"),
    광주광역시("광주광역시"),
    대전광역시("대전광역시"),
    울산광역시("울산광역시"),
    경기도("경기도"),
    충청북도("충청북도"),
    충청남도("충청남도"),
    전라남도("전라남도"),
    경상북도("경상북도"),
    경상남도("경상남도"),
    제주특별자치도("제주특별자치도"),
    강원특별자치도("강원특별자치도"),
    전북특별자치도("전북특별자치도"),
    세종특별자치시("세종특별자치시"),
    국외사업자("국외사업자");

    private final String value;

    City(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static City fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("City 값이 존재하지않습니다.");
        }
        for (City city : City.values()) {
            if (city.value.equalsIgnoreCase(value)) {
                return city;
            }
        }
        throw new IllegalArgumentException("누락된 city: " + value);
    }
}