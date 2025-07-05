package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberApplicationService {

    private static final Logger log = LoggerFactory.getLogger(MemberApplicationService.class);

    private final MemberDomainService memberDomainService;
    private final AuthTokenService authTokenService;
    private final RateLimitServiceInterface rateLimitService;
    private final MemberCacheService memberCacheService;
    private final PasswordEncoder passwordEncoder;
    private final MemberPasswordService memberPasswordService;

    @Autowired
    public MemberApplicationService(MemberDomainService memberDomainService,
                                    AuthTokenService authTokenService,
                                    RateLimitServiceInterface rateLimitService,
                                    @Autowired(required = false) MemberCacheService memberCacheService,
                                    PasswordEncoder passwordEncoder,
                                    MemberPasswordService memberPasswordService) {
        this.memberDomainService = memberDomainService;
        this.authTokenService = authTokenService;
        this.rateLimitService = rateLimitService;
        this.memberCacheService = memberCacheService;
        this.passwordEncoder = passwordEncoder;
        this.memberPasswordService = memberPasswordService;
    }

    @Transactional
    public void changePassword(Long memberId, MemberPasswordChangeRequest request) {
        memberPasswordService.changePassword(memberId, request);
        log.info("비밀번호 변경 완료 - memberId: {}", memberId);
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

    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        rateLimitService.checkRateLimit(request.getUsername(), "join");
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = memberDomainService.createMember(
                request.getUsername(),
                encodedPassword,
                request.getNickname(),
                request.getEmail()
        );
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.cacheMemberResponse(response);
        }
        log.info("회원 가입 완료 - username: {}, id: {}", member.getUsername(), member.getId());
        return response;
    }

    @Transactional
    public MemberLoginResponse login(MemberLoginRequest request, String clientIp) {
        log.info("로그인 시도: username={}", request.getUsername());

        rateLimitService.checkRateLimit(clientIp, "login");

        Member member = memberDomainService.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 사용자: {}", request.getUsername());
                    return new BusinessException(ErrorCode.INVALID_CREDENTIALS);
                });

        log.info("사용자 조회 성공: username={}, status={}, role={}",
                member.getUsername(), member.getStatus(), member.getRole());
        log.debug("저장된 암호화된 비밀번호: {}", member.getPassword());
        log.debug("입력받은 평문 비밀번호: {}", request.getPassword());
        log.info("입력받은 평문 비밀번호: {}", request.getPassword());

        validateMemberStatus(member);

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), member.getPassword());
        log.info("비밀번호 매칭 결과: {}", passwordMatches);

        if (!passwordMatches) {
            log.warn("비밀번호 불일치: username={}", member.getUsername());
            memberDomainService.handleLoginFailure(member);
            if (memberCacheService != null) {
                memberCacheService.evictMemberCache(member.getId());
            }
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        memberDomainService.handleLoginSuccess(member);
        MemberResponse memberResponse = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.cacheMemberResponse(memberResponse);
        }
        String accessToken = authTokenService.generateAccessToken(member);
        String refreshToken = authTokenService.generateRefreshToken(member);
        log.info("로그인 성공 - username: {}, id: {}", member.getUsername(), member.getId());
        return MemberLoginResponse.builder()
                .member(memberResponse)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .apiKey(member.getApiKey())
                .build();
    }

    public MemberResponse getCurrentMemberInfo(Long memberId) {
        log.debug("현재 사용자 정보 조회 요청 - ID: {}", memberId);
        if (memberCacheService != null) {
            MemberResponse cachedMember = memberCacheService.getMemberFromCache(memberId);
            if (cachedMember != null) {
                log.debug("캐시에서 현재 사용자 정보 조회 성공 - ID: {}", memberId);
                return cachedMember;
            }
        }
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.cacheMemberResponse(response);
        }
        log.debug("DB에서 현재 사용자 정보 조회 후 캐시 저장 - ID: {}", memberId);
        return response;
    }

    public MemberResponse getMemberProfile(Long memberId) {
        log.debug("회원 프로필 조회 요청 - ID: {}", memberId);
        if (memberCacheService != null) {
            MemberResponse cachedProfile = memberCacheService.getMemberProfileFromCache(memberId);
            if (cachedProfile != null) {
                log.debug("캐시에서 회원 프로필 조회 성공 - ID: {}", memberId);
                return cachedProfile;
            }
        }
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.cacheMemberProfile(response);
        }
        log.debug("DB에서 회원 프로필 조회 후 캐시 저장 - ID: {}", memberId);
        return response;
    }

    public MemberResponse getMemberInfo(Long memberId) {
        log.debug("관리자 회원 정보 조회 요청 - ID: {}", memberId);
        if (memberCacheService != null) {
            MemberResponse cachedMember = memberCacheService.getMemberFromCache(memberId);
            if (cachedMember != null) {
                log.debug("캐시에서 관리자 회원 정보 조회 성공 - ID: {}", memberId);
                return cachedMember;
            }
        }
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.cacheMemberResponse(response);
        }
        log.debug("DB에서 관리자 회원 정보 조회 후 캐시 저장 - ID: {}", memberId);
        return response;
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
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
            memberCacheService.cacheMemberResponse(response);
        }
        log.info("회원 승인 완료 - ID: {}, approver: {}", memberId, approverId);
        return response;
    }

    @Transactional
    public MemberResponse rejectMember(Long memberId) {
        Member member = memberDomainService.rejectMember(memberId);
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
            memberCacheService.cacheMemberResponse(response);
        }
        log.info("회원 거부 완료 - ID: {}", memberId);
        return response;
    }

    @Transactional
    public MemberResponse suspendMember(Long memberId) {
        Member member = memberDomainService.suspendMember(memberId);
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
            memberCacheService.cacheMemberResponse(response);
        }
        log.warn("회원 정지 완료 - ID: {}", memberId);
        return response;
    }

    @Transactional
    public MemberResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberDomainService.updateMemberProfile(
                memberId,
                request.getNickname(),
                request.getEmail()
        );
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
            memberCacheService.cacheMemberResponse(response);
            memberCacheService.cacheMemberProfile(response);
        }
        log.info("회원 프로필 업데이트 완료 - ID: {}, nickname: {}", memberId, request.getNickname());
        return response;
    }

    @Transactional
    public MemberResponse changeRole(Long memberId, Role role) {
        Member member = memberDomainService.changeRole(memberId, role);
        MemberResponse response = MemberResponse.from(member);
        if (memberCacheService != null) {
            memberCacheService.evictMemberCache(memberId);
            memberCacheService.cacheMemberResponse(response);
        }
        log.info("회원 권한 변경 완료 - ID: {}, role: {}", memberId, role);
        return response;
    }

    public MemberCacheService.CacheStatistics getCacheStatistics() {
        return memberCacheService != null ? memberCacheService.getCacheStatistics() : null;
    }

    @Transactional
    public void evictAllMemberCache() {
        if (memberCacheService != null) {
            memberCacheService.evictAllMemberCache();
            log.warn("관리자에 의한 전체 회원 캐시 무효화 실행");
        }
    }

    private void validateMemberStatus(Member member) {
        if (!member.isActive()) {
            if (member.getStatus() == MemberStatus.PENDING) {
                throw new BusinessException(ErrorCode.MEMBER_NOT_APPROVED);
            } else if (member.isLocked()) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            } else {
                throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
            }
        }
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberDomainService.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.withdraw();
        memberDomainService.save(member);
    }
}