package com.antock.api.member.domain.event;

import com.antock.global.common.domain.BaseDomainEvent;
import lombok.Getter;

@Getter
public class MemberSuspendedEvent extends BaseDomainEvent {
    private final Long memberId;
    private final String username;
    private final String reason;

    public MemberSuspendedEvent(Long memberId, String username, String reason) {
        super();
        this.memberId = memberId;
        this.username = username;
        this.reason = reason;
    }
}

