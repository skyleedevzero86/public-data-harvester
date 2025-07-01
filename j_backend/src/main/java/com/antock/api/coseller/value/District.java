package com.antock.api.coseller.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum District {
    강남구("강남구"),
    강동구("강동구"),
    강북구("강북구"),
    강서구("강서구");

    private final String value;

    District(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static District fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("District 값이 존재하지 않습니다.");
        }
        for (District district : District.values()) {
            if (district.value.equalsIgnoreCase(value)) {
                return district;
            }
        }
        throw new IllegalArgumentException("district 누락: " + value);
    }
}