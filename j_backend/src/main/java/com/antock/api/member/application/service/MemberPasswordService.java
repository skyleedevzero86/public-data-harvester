package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.domain.Member;
import com.antock.api.member.domain.MemberPasswordHistory;
import com.antock.api.member.infrastructure.MemberPasswordHistoryRepository;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPasswordService {

    private final MemberRepository memberRepository;
    private final MemberPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_DAILY_PASSWORD_CHANGES = 3;
    private static final int PASSWORD_HISTORY_CHECK_COUNT = 5;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Transactional
    public void changePassword(Long memberId, MemberPasswordChangeRequest request) {
        log.info("비밀번호 변경 시작 - memberId: {}", memberId);

        validatePasswordChangeRequest(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        log.info("회원 조회 완료 - memberId: {}, username: {}", memberId, member.getUsername());

        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: memberId={}", memberId);
            throw new BusinessException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        long todayChangeCount = getTodayPasswordChangeCount(memberId);
        log.info("오늘 비밀번호 변경 횟수: {}/{}", todayChangeCount, MAX_DAILY_PASSWORD_CHANGES);

        if (todayChangeCount >= MAX_DAILY_PASSWORD_CHANGES) {
            log.warn("비밀번호 변경 실패 - 일일 변경 한도 초과: memberId={}, count={}",
                    memberId, todayChangeCount);
            throw new BusinessException(ErrorCode.DAILY_PASSWORD_CHANGE_LIMIT_EXCEEDED);
        }

        validatePasswordHistory(memberId, request.getNewPassword());

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        log.info("새 비밀번호 인코딩 완료 - memberId: {}", memberId);

        MemberPasswordHistory passwordHistory = MemberPasswordHistory.builder()
                .member(member)
                .passwordHash(member.getPassword())
                .build();
        passwordHistoryRepository.save(passwordHistory);

        log.info("비밀번호 히스토리 저장 완료 - memberId: {}, historyId: {}",
                memberId, passwordHistory.getId());

        member.changePassword(encodedNewPassword);
        Member savedMember = memberRepository.save(member);

        log.info("비밀번호 변경 완료 - memberId: {}, passwordChangedAt: {}",
                memberId, savedMember.getPasswordChangedAt());
    }

    private void validatePasswordChangeRequest(MemberPasswordChangeRequest request) {
        log.debug("비밀번호 변경 요청 검증 시작");

        if (request.getNewPassword() == null || request.getNewPasswordConfirm() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            log.warn("비밀번호 확인 불일치");
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        if (request.getOldPassword() != null && request.getOldPassword().equals(request.getNewPassword())) {
            log.warn("기존 비밀번호와 동일");
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        if (!isValidPassword(request.getNewPassword())) {
            log.warn("비밀번호 정책 위반 - 영문 대/소문자, 숫자, 특수문자를 포함해야 함");
            throw new BusinessException(ErrorCode.WEAK_PASSWORD);
        }

        log.debug("비밀번호 변경 요청 검증 완료");
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private void validatePasswordHistory(Long memberId, String newPassword) {
        log.debug("비밀번호 이력 검증 시작 - memberId: {}", memberId);

        List<MemberPasswordHistory> recentPasswords = passwordHistoryRepository
                .findRecentPasswordHistoryWithLimit(memberId, PASSWORD_HISTORY_CHECK_COUNT);

        log.info("최근 비밀번호 이력 조회 완료 - memberId: {}, count: {}",
                memberId, recentPasswords.size());

        for (MemberPasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                log.warn("비밀번호 변경 실패 - 최근 사용된 비밀번호: memberId={}, historyId={}",
                        memberId, history.getId());
                throw new BusinessException(ErrorCode.RECENTLY_USED_PASSWORD);
            }
        }

        log.debug("비밀번호 이력 검증 완료 - 재사용 없음");
    }

    @Transactional(readOnly = true)
    public boolean isPasswordChangeRequired(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        boolean isRequired = member.isPasswordChangeRequired();

        log.info("비밀번호 변경 필요 여부 확인 - memberId: {}, passwordChangedAt: {}, createDate: {}, isRequired: {}",
                memberId, member.getPasswordChangedAt(), member.getCreateDate(), isRequired);

        return isRequired;
    }

    @Transactional(readOnly = true)
    public boolean isPasswordChangeRecommended(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        boolean isRecommended = member.isPasswordChangeRecommended();

        log.info("비밀번호 변경 권장 여부 확인 - memberId: {}, passwordChangedAt: {}, createDate: {}, isRecommended: {}",
                memberId, member.getPasswordChangedAt(), member.getCreateDate(), isRecommended);

        return isRecommended;
    }

    @Transactional(readOnly = true)
    public long getTodayPasswordChangeCount(Long memberId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long count = passwordHistoryRepository.countPasswordChangesAfter(memberId, startOfDay);

        log.debug("오늘 비밀번호 변경 횟수 조회 - memberId: {}, count: {}", memberId, count);

        return count;
    }

    @Transactional(readOnly = true)
    public List<MemberPasswordHistory> getPasswordHistory(Long memberId, int limit) {
        return passwordHistoryRepository.findRecentPasswordHistoryWithLimit(memberId, limit);
    }
}