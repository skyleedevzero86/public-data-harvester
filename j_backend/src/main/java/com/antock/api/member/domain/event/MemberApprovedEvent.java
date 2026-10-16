package com.antock.api.member.domain.event;

import com.antock.global.common.domain.BaseDomainEvent;
import lombok.Getter;

@Getter
public class MemberApprovedEvent extends BaseDomainEvent {
    private final Long memberId;
    private final String username;
    private final String email;
    private final Long approverId;

    public MemberApprovedEvent(Long memberId, String username, String email, Long approverId) {
        super();
        this.memberId = memberId;
        this.username = username;
        this.email = email;
        this.approverId = approverId;
    }
}

