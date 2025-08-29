package com.antock.api.member.application.service;

import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDomainService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member createMember(String username, String password, String nickname, String email) {
        validateDuplicateUsername(username);
        validateDuplicateEmail(email);

        String encodedPassword = passwordEncoder.encode(password);
        log.info("회원가입 - 비밀번호 암호화 완료: username={}", username);

        Member member = Member.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(nickname)
                .email(email)
                .apiKey(generateApiKey())
                .status(MemberStatus.PENDING)
                .role(Role.USER)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("회원가입 완료: username={}, memberId={}, status={}",
                username, savedMember.getId(), savedMember.getStatus());

        return savedMember;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByUsername(String username) {
        log.debug("DB에서 사용자 조회 시작: username={}", username);

        Optional<Member> memberOpt = memberRepository.findByUsername(username);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            log.debug("사용자 조회 성공: username={}, id={}, status={}, role={}",
                    username, member.getId(), member.getStatus(), member.getRole());
        } else {
            log.debug("사용자를 찾을 수 없음: username={}", username);
        }

        return memberOpt;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public Page<Member> findMembersByStatus(MemberStatus status, Pageable pageable) {
        return memberRepository.findByStatus(status, pageable);
    }

    public Page<Member> findAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public List<Member> findPendingMembers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return memberRepository.findPendingMembersAfter(MemberStatus.PENDING, thirtyDaysAgo);
    }

    @Transactional
    public Member approveMember(Long memberId, Long approverId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.approve(approverId);
        Member savedMember = memberRepository.save(member);

        log.info("회원 승인 완료: memberId={}, approverId={}, status={}",
                memberId, approverId, savedMember.getStatus());

        return savedMember;
    }

    @Transactional
    public Member rejectMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.reject();
        return memberRepository.save(member);
    }

    @Transactional
    public Member suspendMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.suspend();
        return memberRepository.save(member);
    }

    @Transactional
    public Member updateMemberProfile(Long memberId, String nickname, String email) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.getEmail().equals(email)) {
            validateDuplicateEmail(email);
        }

        member.updateProfile(nickname, email);
        return memberRepository.save(member);
    }

    @Transactional
    public Member changeRole(Long memberId, Role role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.changeRole(role);
        return memberRepository.save(member);
    }

    @Transactional
    public Member unlockMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            member.approve(null);
        }

        if (member.isLocked()) {
            member.unlock();
        }

        return memberRepository.save(member);
    }

    @Transactional
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Member saveAndFlush(Member member) {
        return memberRepository.saveAndFlush(member);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByUsernameNoCache(String username) {
        log.debug("캐시 없이 DB에서 사용자 조회: username={}", username);

        Optional<Member> memberOpt = memberRepository.findByUsername(username);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            log.debug("사용자 조회 성공 (캐시 없음): username={}, id={}, status={}, role={}, loginFailCount={}",
                    username, member.getId(), member.getStatus(), member.getRole(), member.getLoginFailCount());

            if (member.isLocked()) {
                log.warn("계정 잠금 상태: username={}, lockedAt={}", username, member.getAccountLockedAt());
            }
        } else {
            log.debug("사용자를 찾을 수 없음 (캐시 없음): username={}", username);
        }

        return memberOpt;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Member forceUpdateLoginFailCount(Member member) {
        try {
            log.debug("로그인 실패 카운트 강제 업데이트 시작: memberId={}, currentFailCount={}",
                    member.getId(), member.getLoginFailCount());

            Integer currentFailCountFromDb = memberRepository.getCurrentLoginFailCount(member.getId());
            log.debug("DB에서 조회된 현재 실패 카운트: {}", currentFailCountFromDb);

            member.increaseLoginFailCount();

            Integer newFailCount = member.getLoginFailCount();
            String status = member.getStatus().name();
            LocalDateTime lockedAt = member.getAccountLockedAt();

            int updatedRows = memberRepository.updateLoginFailBySql(
                    member.getId(),
                    newFailCount,
                    status,
                    lockedAt);

            log.info("SQL 직접 업데이트 완료: memberId={}, updatedRows={}, newFailCount={}, status={}, lockedAt={}",
                    member.getId(), updatedRows, newFailCount, status, lockedAt);

            return member;

        } catch (Exception e) {
            log.error("로그인 실패 카운트 강제 업데이트 실패: memberId={}, error={}",
                    member.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Integer getCurrentLoginFailCount(Long memberId) {
        return memberRepository.getCurrentLoginFailCount(memberId);
    }

    @Transactional
    public void forceUpdateLoginFailCountBySql(Long memberId, Integer failCount, String status,
                                               LocalDateTime lockedAt) {
        try {
            log.debug("SQL로 로그인 실패 카운트 업데이트: memberId={}, failCount={}, status={}, lockedAt={}",
                    memberId, failCount, status, lockedAt);

            int updatedRows = memberRepository.updateLoginFailBySql(memberId, failCount, status, lockedAt);

            log.info("SQL 업데이트 완료: memberId={}, updatedRows={}, failCount={}, status={}",
                    memberId, updatedRows, failCount, status);

            if (updatedRows == 0) {
                log.warn("업데이트된 행이 없음: memberId={}", memberId);
            }
        } catch (Exception e) {
            log.error("SQL 업데이트 실패: memberId={}, error={}", memberId, e.getMessage(), e);
            throw e;
        }
    }

    public long countMembersByStatus(MemberStatus status) {
        return memberRepository.countByStatus(status);
    }

    private void validateDuplicateUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLoginFailureInNewTransaction(Long memberId, Integer currentFailCount) {
        try {
            log.debug("새 트랜잭션에서 로그인 실패 처리: memberId={}, currentFailCount={}",
                    memberId, currentFailCount);

            Integer newFailCount = currentFailCount + 1;
            String status = MemberStatus.APPROVED.name();
            LocalDateTime lockedAt = null;

            if (newFailCount >= 5) {
                status = MemberStatus.SUSPENDED.name();
                lockedAt = LocalDateTime.now();
                log.warn("계정 잠금 처리: memberId={}, failCount={}", memberId, newFailCount);
            }

            int updatedRows = memberRepository.updateLoginFailBySql(memberId, newFailCount, status, lockedAt);
            log.info("로그인 실패 처리 완료: memberId={}, updatedRows={}, newFailCount={}, status={}",
                    memberId, updatedRows, newFailCount, status);

        } catch (Exception e) {
            log.error("로그인 실패 처리 중 오류: memberId={}, error={}", memberId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLoginSuccessInNewTransaction(Long memberId) {
        try {
            log.debug("새 트랜잭션에서 로그인 성공 처리: memberId={}", memberId);

            int updatedRows = memberRepository.updateLoginSuccessBySql(memberId, LocalDateTime.now());

            log.info("로그인 성공 처리 완료: memberId={}, updatedRows={}", memberId, updatedRows);

        } catch (Exception e) {
            log.error("로그인 성공 처리 중 오류: memberId={}, error={}", memberId, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Member> findMembersByStatusAndRole(String statusStr, String roleStr, Pageable pageable) {
        MemberStatus status = null;
        Role role = null;

        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = MemberStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status parameter: {}", statusStr);
            }
        }

        if (roleStr != null && !roleStr.trim().isEmpty()) {
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role parameter: {}", roleStr);
            }
        }

        return memberRepository.findByStatusAndRole(status, role, pageable);
    }

    @Transactional
    public Member resetToPending(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.resetToPending();
        return memberRepository.save(member);
    }

    public long countAllMembers() {
        return memberRepository.count();
    }
}