package com.antock.api.health.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HealthStatus {
    UP("UP", "정상"),
    DOWN("DOWN", "장애"),
    UNKNOWN("UNKNOWN", "알 수 없음");

    private final String code;
    private final String description;

    HealthStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static HealthStatus fromCode(String code) {
        for (HealthStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
