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
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>_+=\\-\\[\\]\\\\;'`~])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>_+=\\-\\[\\]\\\\;'`~]{8,}$");

    @Transactional
    public void changePassword(Long memberId, MemberPasswordChangeRequest request) {
        validatePasswordChangeRequest(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        long todayChangeCount = getTodayPasswordChangeCount(memberId);

        if (todayChangeCount >= MAX_DAILY_PASSWORD_CHANGES) {
            throw new BusinessException(ErrorCode.DAILY_PASSWORD_CHANGE_LIMIT_EXCEEDED);
        }

        validatePasswordHistory(memberId, request.getNewPassword());

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

        MemberPasswordHistory passwordHistory = MemberPasswordHistory.builder()
                .member(member)
                .passwordHash(member.getPassword())
                .build();
        passwordHistoryRepository.save(passwordHistory);

        member.changePassword(encodedNewPassword);
        memberRepository.save(member);
    }

    private void validatePasswordChangeRequest(MemberPasswordChangeRequest request) {
        if (request.getNewPassword() == null || request.getNewPasswordConfirm() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        if (request.getOldPassword() != null && request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        if (!isValidPassword(request.getNewPassword())) {
            throw new BusinessException(ErrorCode.WEAK_PASSWORD);
        }
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private void validatePasswordHistory(Long memberId, String newPassword) {
        List<MemberPasswordHistory> recentPasswords = passwordHistoryRepository
                .findRecentPasswordHistoryWithLimit(memberId, PASSWORD_HISTORY_CHECK_COUNT);

        for (MemberPasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new BusinessException(ErrorCode.RECENTLY_USED_PASSWORD);
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean isPasswordChangeRequired(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return member.isPasswordChangeRequired();
    }

    @Transactional(readOnly = true)
    public boolean isPasswordChangeRecommended(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return member.isPasswordChangeRecommended();
    }

    @Transactional(readOnly = true)
    public long getTodayPasswordChangeCount(Long memberId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return passwordHistoryRepository.countPasswordChangesAfter(memberId, startOfDay);
    }

    @Transactional(readOnly = true)
    public List<MemberPasswordHistory> getPasswordHistory(Long memberId, int limit) {
        return passwordHistoryRepository.findRecentPasswordHistoryWithLimit(memberId, limit);
    }
}