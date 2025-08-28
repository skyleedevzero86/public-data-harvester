package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.dto.response.PasswordStatusResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.domain.MemberPasswordHistory;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.infrastructure.MemberPasswordHistoryRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.antock.global.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MemberApplicationService {

    private final MemberDomainService memberDomainService;
    private final AuthTokenService authTokenService;
    private final RateLimitServiceInterface rateLimitService;
    private final MemberCacheService memberCacheService;
    private final PasswordEncoder passwordEncoder;
    private final MemberPasswordService memberPasswordService;
    private final Executor asyncExecutor;

    @Autowired
    public MemberApplicationService(MemberDomainService memberDomainService,
            AuthTokenService authTokenService,
            RateLimitServiceInterface rateLimitService,
            @Autowired(required = false) MemberCacheService memberCacheService,
            PasswordEncoder passwordEncoder,
            MemberPasswordService memberPasswordService,
            @Qualifier("applicationTaskExecutor") Executor asyncExecutor) {
        this.memberDomainService = memberDomainService;
        this.authTokenService = authTokenService;
        this.rateLimitService = rateLimitService;
        this.memberCacheService = memberCacheService;
        this.passwordEncoder = passwordEncoder;
        this.memberPasswordService = memberPasswordService;
        this.asyncExecutor = asyncExecutor;
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
    public Long getMemberIdByUsername(String username) {
        return memberDomainService.findByUsername(username)
                .map(Member::getId)
                .orElse(null);
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
    public MemberLoginResponse login(MemberLoginRequest request, String clientIp) {
        log.info("로그인 시도: username={}, clientIp={}", request.getUsername(), clientIp);

        rateLimitService.checkRateLimit(clientIp, "login");

        Member member = memberDomainService.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        validateMemberStatus(member);

        if (!member.matchPassword(request.getPassword())) {
            memberDomainService.handleLoginFailureInNewTransaction(member.getId(), member.getLoginFailCount());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = authTokenService.generateAccessToken(member);
        String refreshToken = authTokenService.generateRefreshToken(member);

        if (memberCacheService != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    memberCacheService.cacheMember(member);
                } catch (Exception e) {
                    log.warn("회원 정보 캐시 저장 실패: {}", e.getMessage());
                }
            }, asyncExecutor);
        }

        log.info("로그인 성공: username={}, id={}", member.getUsername(), member.getId());

        return MemberLoginResponse.builder()
                .member(MemberResponse.from(member))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .apiKey(member.getApiKey())
                .build();
    }

    @Cacheable(value = "member", key = "#memberId")
    public MemberResponse getCurrentMemberInfo(Long memberId) {
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponse.from(member);
    }

    @Cacheable(value = "memberProfile", key = "#memberId")
    public MemberResponse getMemberProfile(Long memberId) {
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponse.from(member);
    }

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

    public MemberCacheService.CacheStatistics getCacheStatistics() {
        return memberCacheService != null ? memberCacheService.getCacheStatistics()
                : new MemberCacheService.CacheStatistics(0, 0, 0, 0.0, 0, false);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile", "memberList", "pendingMembers" }, allEntries = true)
    public void evictAllMemberCache() {
        if (memberCacheService != null) {
            memberCacheService.evictAllMemberCache();
        }
    }

    private void validateMemberStatus(Member member) {
        if (member.getStatus() == MemberStatus.REJECTED) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_APPROVED, "거부된 회원입니다.");
        }

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_APPROVED, "정지된 회원입니다.");
        }

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_APPROVED, "탈퇴한 회원입니다.");
        }

        if (member.getStatus() == MemberStatus.PENDING) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_APPROVED, "승인 대기중인 회원입니다.");
        }

        if (member.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED, "계정이 잠겨있습니다.");
        }
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        memberDomainService.save(member);

        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
        }
    }

    @Transactional(readOnly = true)
    public long countMembersByStatus(MemberStatus status) {
        return memberDomainService.countMembersByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countAllMembers() {
        return memberDomainService.countAllMembers();
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> getMembersByStatusAndRole(String status, String role, Pageable pageable) {
        return memberDomainService.findMembersByStatusAndRole(status, role, pageable)
                .map(MemberResponse::from);
    }

    @Transactional(readOnly = true)
    public PasswordStatusResponse getPasswordStatus(Long memberId) {
        boolean isChangeRequired = memberPasswordService.isPasswordChangeRequired(memberId);
        boolean isChangeRecommended = memberPasswordService.isPasswordChangeRecommended(memberId);
        long todayChangeCount = memberPasswordService.getTodayPasswordChangeCount(memberId);

        return PasswordStatusResponse.of(isChangeRequired, isChangeRecommended, todayChangeCount);
    }

    @Transactional
    @CacheEvict(value = { "member", "memberProfile" }, allEntries = true)
    public MemberResponse resetToPending(Long memberId) {
        Member member = memberDomainService.resetToPending(memberId);
        return MemberResponse.from(member);
    }
}