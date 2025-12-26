package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.dto.response.PasswordStatusResponse;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class MemberApplicationService {

    private final MemberAuthService memberAuthService;
    private final MemberManagementService memberManagementService;
    private final MemberQueryService memberQueryService;
    private final MemberPasswordService memberPasswordService;
    private final MemberCacheService memberCacheService;

    @Autowired
    public MemberApplicationService(MemberAuthService memberAuthService,
                                    MemberManagementService memberManagementService,
                                    MemberQueryService memberQueryService,
                                    MemberPasswordService memberPasswordService,
                                    @Autowired(required = false) MemberCacheService memberCacheService) {
        this.memberAuthService = memberAuthService;
        this.memberManagementService = memberManagementService;
        this.memberQueryService = memberQueryService;
        this.memberPasswordService = memberPasswordService;
        this.memberCacheService = memberCacheService;
    }

    @Transactional
    public void changePassword(Long memberId, MemberPasswordChangeRequest request) {
        memberPasswordService.changePassword(memberId, request);
        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isPasswordChangeRequired(Long memberId) {
        return memberPasswordService.isPasswordChangeRequired(memberId);
    }

    @Transactional(readOnly = true)
    public boolean isPasswordChangeRecommended(Long memberId) {
        return memberPasswordService.isPasswordChangeRecommended(memberId);
    }

    @Transactional(readOnly = true)
    public long getTodayPasswordChangeCount(Long memberId) {
        return memberPasswordService.getTodayPasswordChangeCount(memberId);
    }

    @Transactional(readOnly = true)
    public PasswordStatusResponse getPasswordStatus(Long memberId) {
        boolean isChangeRequired = memberPasswordService.isPasswordChangeRequired(memberId);
        boolean isChangeRecommended = memberPasswordService.isPasswordChangeRecommended(memberId);
        long todayChangeCount = memberPasswordService.getTodayPasswordChangeCount(memberId);

        return PasswordStatusResponse.of(isChangeRequired, isChangeRecommended, todayChangeCount);
    }

    @Transactional
    public MemberLoginResponse login(MemberLoginRequest request, String clientIp) {
        return memberAuthService.login(request, clientIp);
    }

    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        return memberManagementService.join(request);
    }

    public MemberResponse getMemberInfo(Long memberId) {
        return memberQueryService.getMemberInfo(memberId);
    }

    public Page<MemberResponse> getMembers(Pageable pageable) {
        return memberQueryService.getMembers(pageable);
    }

    public Page<MemberResponse> getPendingMembers(Pageable pageable) {
        return memberQueryService.getPendingMembers(pageable);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile", "pendingMembers" }, allEntries = true)
    public MemberResponse approveMember(Long memberId, Long approverId) {
        return memberManagementService.approveMember(memberId, approverId);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile", "pendingMembers" }, allEntries = true)
    public MemberResponse rejectMember(Long memberId) {
        return memberManagementService.rejectMember(memberId);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse suspendMember(Long memberId) {
        return memberManagementService.suspendMember(memberId);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse unlockMember(Long memberId) {
        return memberManagementService.unlockMember(memberId);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, key = "#memberId")
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        return memberManagementService.updateProfile(memberId, request);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse changeRole(Long memberId, Role role) {
        return memberManagementService.changeRole(memberId, role);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse resetToPending(Long memberId) {
        return memberManagementService.resetToPending(memberId);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse changeMemberRole(Long memberId, Role newRole, Long approverId) {
        return memberManagementService.changeMemberRole(memberId, newRole, approverId);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public void deleteMember(Long memberId) {
        memberManagementService.deleteMember(memberId);
    }

    public List<MemberResponse> getAllMembers() {
        return memberQueryService.getAllMembers();
    }

    public Page<MemberResponse> getMembersByStatusAndRole(String status, String role, Pageable pageable) {
        return memberQueryService.getMembersByStatusAndRole(status, role, pageable);
    }

    public Long getMemberIdByUsername(String username) {
        return memberQueryService.getMemberIdByUsername(username);
    }

    public long countMembersByStatus(MemberStatus status) {
        return memberQueryService.countMembersByStatus(status);
    }

    public MemberCacheService.CacheStatistics getCacheStatistics() {
        if (memberCacheService != null) {
            return memberCacheService.getCacheStatistics();
        }
        return MemberCacheService.CacheStatistics.empty();
    }

    public void evictAllMemberCache() {
        log.info("전체 회원 캐시 무효화 요청");

        if (memberCacheService != null) {
            try {
                memberCacheService.evictAllMemberCache();
                log.info("전체 회원 캐시 무효화 완료");
            } catch (Exception e) {
                log.error("전체 회원 캐시 무효화 실패", e);
                throw new RuntimeException("캐시 무효화 중 오류가 발생했습니다: " + e.getMessage(), e);
            }
        } else {
            log.warn("MemberCacheService가 설정되지 않아 캐시 무효화를 수행할 수 없습니다");
        }
    }

}