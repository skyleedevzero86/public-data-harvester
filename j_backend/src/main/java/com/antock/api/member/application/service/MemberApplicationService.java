package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.security.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberApplicationService {

    private final MemberDomainService memberDomainService;
    private final AuthTokenService authTokenService;
    private final RateLimitService rateLimitService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        // Rate Limiting 체크
        rateLimitService.checkRateLimit(request.getUsername(), "join");

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = memberDomainService.createMember(
                request.getUsername(),
                encodedPassword,
                request.getNickname(),
                request.getEmail()
        );

        return MemberResponse.from(member);
    }

    @Transactional
    public MemberLoginResponse login(MemberLoginRequest request, String clientIp) {
        // Rate Limiting 체크
        rateLimitService.checkRateLimit(clientIp, "login");

        Member member = memberDomainService.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 계정 상태 확인
        if (!member.isActive()) {
            if (member.getStatus() == MemberStatus.PENDING) {
                throw new BusinessException(ErrorCode.MEMBER_NOT_APPROVED);
            } else if (member.isLocked()) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            } else {
                throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
            }
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            memberDomainService.handleLoginFailure(member);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 로그인 성공 처리
        memberDomainService.handleLoginSuccess(member);

        // 토큰 생성
        String accessToken = authTokenService.generateAccessToken(member);
        String refreshToken = authTokenService.generateRefreshToken(member);

        return MemberLoginResponse.builder()
                .member(MemberResponse.from(member))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .apiKey(member.getApiKey())
                .build();
    }

    public Page<MemberResponse> getMembers(Pageable pageable) {
        return memberDomainService.findAllMembers(pageable)
                .map(MemberResponse::from);
    }

    public Page<MemberResponse> getPendingMembers(Pageable pageable) {
        return memberDomainService.findMembersByStatus(MemberStatus.PENDING, pageable)
                .map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse approveMember(Long memberId, Long approverId) {
        Member member = memberDomainService.approveMember(memberId, approverId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse rejectMember(Long memberId) {
        Member member = memberDomainService.rejectMember(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse suspendMember(Long memberId) {
        Member member = memberDomainService.suspendMember(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberDomainService.updateMemberProfile(
                memberId,
                request.getNickname(),
                request.getEmail()
        );
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse changeRole(Long memberId, Role role) {
        Member member = memberDomainService.changeRole(memberId, role);
        return MemberResponse.from(member);
    }

    public MemberResponse getMemberInfo(Long memberId) {
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }
}