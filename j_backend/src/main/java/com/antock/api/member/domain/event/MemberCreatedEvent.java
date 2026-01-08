package com.antock.api.member.domain.event;

import com.antock.global.common.domain.BaseDomainEvent;
import lombok.Getter;

@Getter
public class MemberCreatedEvent extends BaseDomainEvent {
    private final Long memberId;
    private final String username;
    private final String email;

    public MemberCreatedEvent(Long memberId, String username, String email) {
        super();
        this.memberId = memberId;
        this.username = username;
        this.email = email;
    }
}

