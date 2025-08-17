package com.antock.api.member.application.dto.response;

import com.antock.api.member.value.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatsDto {
    private MemberStatus status;
    private Long count;
}