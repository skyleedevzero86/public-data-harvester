package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberDomainService memberDomainService;

    @Cacheable(value = "member", key = "#memberId")
    public MemberResponse getMemberInfo(Long memberId) {
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    @Cacheable(value = "memberList", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MemberResponse> getMembers(Pageable pageable) {
        return memberDomainService.findAllMembers(pageable)
                .map(MemberResponse::from);
    }

    @Cacheable(value = "pendingMembers", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MemberResponse> getPendingMembers(Pageable pageable) {
        return memberDomainService.findMembersByStatus(MemberStatus.PENDING, pageable)
                .map(MemberResponse::from);
    }

    public List<MemberResponse> getAllMembers() {
        return memberDomainService.findAllMembers()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    public Page<MemberResponse> getMembersByStatusAndRole(String status, String role, Pageable pageable) {
        return memberDomainService.findMembersByStatusAndRole(status, role, pageable)
                .map(MemberResponse::from);
    }

    public Long getMemberIdByUsername(String username) {
        return memberDomainService.findByUsername(username)
                .map(Member::getId)
                .orElse(null);
    }

    public long countMembersByStatus(MemberStatus status) {
        return memberDomainService.countMembersByStatus(status);
    }
}

