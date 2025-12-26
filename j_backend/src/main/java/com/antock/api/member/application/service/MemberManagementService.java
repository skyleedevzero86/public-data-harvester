package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MemberManagementService {

    private final MemberDomainService memberDomainService;
    private final MemberCacheService memberCacheService;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberManagementService(MemberDomainService memberDomainService,
                                   @Autowired(required = false) MemberCacheService memberCacheService,
                                   MemberRepository memberRepository) {
        this.memberDomainService = memberDomainService;
        this.memberCacheService = memberCacheService;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        log.info("회원가입 요청: username={}", request.getUsername());

        if (memberDomainService.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        if (memberDomainService.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = memberDomainService.createMember(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getEmail());

        log.info("회원가입 완료: username={}, id={}", member.getUsername(), member.getId());

        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile", "pendingMembers" }, allEntries = true)
    public MemberResponse approveMember(Long memberId, Long approverId) {
        Member member = memberDomainService.approveMember(memberId, approverId);
        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile", "pendingMembers" }, allEntries = true)
    public MemberResponse rejectMember(Long memberId) {
        Member member = memberDomainService.rejectMember(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse suspendMember(Long memberId) {
        Member member = memberDomainService.suspendMember(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse unlockMember(Long memberId) {
        Member member = memberDomainService.unlockMember(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, key = "#memberId")
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberDomainService.updateMemberProfile(
                memberId, request.getNickname(), request.getEmail());

        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
        }

        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse changeRole(Long memberId, Role role) {
        Member member = memberDomainService.changeRole(memberId, role);
        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse resetToPending(Long memberId) {
        Member member = memberDomainService.resetToPending(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse changeMemberRole(Long memberId, Role newRole, Long approverId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() == newRole) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 동일한 역할입니다.");
        }

        Member approver = memberRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (approver.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "역할 변경 권한이 없습니다.");
        }

        member.changeRole(newRole);

        Member savedMember = memberRepository.save(member);

        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(savedMember.getId());
        }

        log.info("회원 역할 변경 완료 - memberId: {}, oldRole: {}, newRole: {}, approver: {}",
                memberId, member.getRole(), newRole, approver.getUsername());

        return MemberResponse.from(savedMember);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public void deleteMember(Long memberId) {
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRole() == Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자는 삭제할 수 없습니다.");
        }

        member.withdraw();
        memberDomainService.save(member);

        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
        }

        log.info("회원 삭제 완료: username={}, id={}", member.getUsername(), memberId);
    }
}

