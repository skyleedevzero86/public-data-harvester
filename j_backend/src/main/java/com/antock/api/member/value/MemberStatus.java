package com.antock.api.member.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
    PENDING("승인 대기"),
    APPROVED("승인됨"),
    REJECTED("거부됨"),
    SUSPENDED("정지됨"),
    WITHDRAWN("탈퇴됨");

    private final String description;
}

