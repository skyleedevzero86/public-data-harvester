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

    @Transactional
    public Member createMember(String username, String password, String nickname, String email) {
        validateDuplicateUsername(username);
        validateDuplicateEmail(email);

        Member member = Member.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .email(email)
                .apiKey(generateApiKey())
                .status(MemberStatus.PENDING)
                .role(Role.USER)
                .build();

        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByUsername(String username) {
        log.debug("DB에서 사용자 조회 시작: username={}", username);

        Optional<Member> memberOpt = memberRepository.findByUsername(username);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            log.debug("DB 조회 결과 - memberId: {}, loginFailCount: {}, status: {}",
                    member.getId(), member.getLoginFailCount(), member.getStatus());
        } else {
            log.debug("DB에서 사용자를 찾을 수 없음: username={}", username);
        }

        return memberOpt;
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

    public List<Member> findPendingMembers() {
        return memberRepository.findPendingMembersAfter(
                MemberStatus.PENDING,
                LocalDateTime.now().minusDays(30)
        );
    }

    @Transactional
    public Member approveMember(Long memberId, Long approverId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() != MemberStatus.PENDING && member.getStatus() != MemberStatus.REJECTED) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
        }

        member.approve(approverId);
        return memberRepository.save(member);
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
        return memberRepository.saveAndFlush(member);
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

        log.info("계정 정지 해제 시작 - memberId: {}, 현재 상태: {}, 실패 횟수: {}, 정지 시간: {}",
                memberId, member.getStatus(), member.getLoginFailCount(), member.getAccountLockedAt());

        Integer beforeFailCount = member.getLoginFailCount();
        MemberStatus beforeStatus = member.getStatus();

        member.resetLoginFailCount();
        Member savedMember = memberRepository.saveAndFlush(member);

        log.info("계정 정지 해제 완료 - memberId: {}, 상태: {} -> {}, 실패 횟수: {} -> {}, 정지 시간: {}",
                memberId, beforeStatus, savedMember.getStatus(), beforeFailCount,
                savedMember.getLoginFailCount(), savedMember.getAccountLockedAt());

        return savedMember;
    }

    @Transactional
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public Member saveAndFlush(Member member) {
        log.info("saveAndFlush 시작 - memberId: {}, loginFailCount: {}, status: {}",
                member.getId(), member.getLoginFailCount(), member.getStatus());

        try {

            Member savedMember = memberRepository.saveAndFlush(member);

            log.info("saveAndFlush 1단계 완료 - 저장된 loginFailCount: {}, status: {}",
                    savedMember.getLoginFailCount(), savedMember.getStatus());

            memberRepository.flush();

            Member reloadedMember = memberRepository.findById(member.getId()).orElse(null);
            if (reloadedMember != null) {
                log.info("saveAndFlush 2단계 - 재조회 결과: loginFailCount={}, status={}",
                        reloadedMember.getLoginFailCount(), reloadedMember.getStatus());
                return reloadedMember;
            }

            return savedMember;

        } catch (Exception e) {
            log.error("saveAndFlush 실패 - memberId: {}, error: {}", member.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByUsernameNoCache(String username) {
        log.info("캐시 우회 DB 직접 조회: username={}", username);

        Optional<Member> memberOpt = memberRepository.findByUsername(username);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            log.info("DB 직접 조회 결과 - memberId: {}, loginFailCount: {}, status: {}",
                    member.getId(), member.getLoginFailCount(), member.getStatus());
        } else {
            log.warn("DB에서 사용자를 찾을 수 없음: username={}", username);
        }

        return memberOpt;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Member forceUpdateLoginFail(Member member) {
        log.error("강제 로그인 실패 업데이트 시작 - memberId: {}", member.getId());

        try {
            Member currentMember = memberRepository.findById(member.getId()).orElse(null);
            if (currentMember != null) {
                log.warn("업데이트 전 DB 상태 - loginFailCount: {}, status: {}",
                        currentMember.getLoginFailCount(), currentMember.getStatus());
            }

            Integer beforeCount = member.getLoginFailCount();
            log.warn("메모리 상태 - Before: {}, After: {}", beforeCount, member.getLoginFailCount());

            Member savedMember = memberRepository.saveAndFlush(member);
            log.error("1차 저장 완료 - savedFailCount: {}", savedMember.getLoginFailCount());

            memberRepository.flush();

            Member finalMember = memberRepository.findById(member.getId()).orElse(null);
            if (finalMember != null) {
                log.error("최종 DB 확인 - memberId: {}, loginFailCount: {}, status: {}, lockedAt: {}",
                        finalMember.getId(), finalMember.getLoginFailCount(),
                        finalMember.getStatus(), finalMember.getAccountLockedAt());

                return finalMember;
            }

            return savedMember;

        } catch (Exception e) {
            log.error("강제 업데이트 실패 - memberId: {}, error: {}", member.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Integer getCurrentLoginFailCount(Long memberId) {
        Integer count = memberRepository.getCurrentLoginFailCount(memberId);
        log.debug("DB에서 현재 로그인 실패 횟수 조회 - memberId: {}, count: {}", memberId, count);
        return count;
    }

    @Transactional
    public void forceUpdateLoginFailCountBySql(Long memberId, Integer failCount, String status, LocalDateTime lockedAt) {
        log.error("원시 SQL로 강제 업데이트 - memberId: {}, failCount: {}, status: {}", memberId, failCount, status);

        try {

            Integer beforeCount = memberRepository.getCurrentLoginFailCount(memberId);
            log.warn("업데이트 전 DB 값: {}", beforeCount);

            int updatedRows = memberRepository.updateLoginFailBySql(memberId, failCount, status, lockedAt);
            log.error("원시 SQL 업데이트 완료 - 영향받은 행: {}", updatedRows);

            Integer afterCount = memberRepository.getCurrentLoginFailCount(memberId);
            log.error("업데이트 후 DB 값: {} -> {}", beforeCount, afterCount);

            if (!failCount.equals(afterCount)) {
                log.error("업데이트 실패! 예상: {}, 실제: {}", failCount, afterCount);
            } else {
                log.info("업데이트 성공 확인");
            }

        } catch (Exception e) {
            log.error("원시 SQL 업데이트 실패: {}", e.getMessage(), e);
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
        log.error("별도 트랜잭션에서 로그인 실패 처리 시작 - memberId: {}", memberId);

        try {

            Integer newFailCount = (currentFailCount != null ? currentFailCount : 0) + 1;
            log.warn("새로운 실패 횟수 계산: {} -> {}", currentFailCount, newFailCount);

            String newStatus = "APPROVED";
            LocalDateTime lockedAt = null;
            if (newFailCount >= 5) {
                newStatus = "SUSPENDED";
                lockedAt = LocalDateTime.now();
                log.error("5회 실패로 계정 정지 처리 ");
            }

            int updatedRows = memberRepository.updateLoginFailBySql(memberId, newFailCount, newStatus, lockedAt);
            log.error("원시 SQL 업데이트 완료 - 영향받은 행: {}", updatedRows);

            Integer finalDbFailCount = memberRepository.getCurrentLoginFailCount(memberId);
            log.error("===== 최종 확인 - DB 실패 횟수: {} =====", finalDbFailCount);

            if (newFailCount >= 5) {
                log.error(" 계정 자동 정지 완료 - memberId: {}, 실패 횟수: {}",
                        memberId, finalDbFailCount);
            }

            memberRepository.flush();

        } catch (Exception e) {
            log.error("별도 트랜잭션 로그인 실패 처리 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLoginSuccessInNewTransaction(Long memberId) {
        log.info("별도 트랜잭션에서 로그인 성공 처리 시작 - memberId: {}", memberId);

        try {
            int updatedRows = memberRepository.updateLoginFailBySql(memberId, 0, "APPROVED", null);
            log.info("성공 처리 SQL 업데이트 완료 - 영향받은 행: {}", updatedRows);

            memberRepository.flush();

        } catch (Exception e) {
            log.error("별도 트랜잭션 로그인 성공 처리 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<Member> findMembersByStatusAndRole(String statusStr, String roleStr, Pageable pageable) {
        MemberStatus status = null;
        Role role = null;


        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = MemberStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상태 값: {}", statusStr);
            }
        }

        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 권한 값: {}", roleStr);
            }
        }

        return memberRepository.findByStatusAndRole(status, role, pageable);
    }

}