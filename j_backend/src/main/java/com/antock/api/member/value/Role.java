package com.antock.api.member.value;

public enum Role {
    USER("일반 사용자"),
    MANAGER("관리자"),
    ADMIN("시스템 관리자");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}