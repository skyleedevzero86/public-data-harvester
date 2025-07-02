package com.antock.api.member.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("일반 사용자"),
    MANAGER("관리자"),
    ADMIN("시스템 관리자");

    private final String description;
}