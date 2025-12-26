package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class MemberAuthService {

    private final MemberDomainService memberDomainService;
    private final AuthTokenService authTokenService;
    private final RateLimitServiceInterface rateLimitService;
    private final MemberCacheService memberCacheService;
    private final Executor asyncExecutor;

    @Autowired
    public MemberAuthService(MemberDomainService memberDomainService,
                             AuthTokenService authTokenService,
                             RateLimitServiceInterface rateLimitService,
                             @Autowired(required = false) MemberCacheService memberCacheService,
                             @org.springframework.beans.factory.annotation.Qualifier("applicationTaskExecutor") Executor asyncExecutor) {
        this.memberDomainService = memberDomainService;
        this.authTokenService = authTokenService;
        this.rateLimitService = rateLimitService;
        this.memberCacheService = memberCacheService;
        this.asyncExecutor = asyncExecutor;
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

        member.updateLastLoginAt(LocalDateTime.now());
        memberDomainService.save(member);

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

        log.info("로그인 성공: username={}, id={}, lastLoginAt={}",
                member.getUsername(), member.getId(), member.getLastLoginAt());

        return MemberLoginResponse.builder()
                .member(MemberResponse.from(member))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .apiKey(member.getApiKey())
                .build();
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
}

