package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.PasswordFindRequest;
import com.antock.api.member.application.dto.request.PasswordResetRequest;
import com.antock.api.member.application.dto.response.PasswordFindResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.domain.MemberPasswordHistory;
import com.antock.api.member.domain.PasswordResetToken;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.infrastructure.MemberPasswordHistoryRepository;
import com.antock.api.member.infrastructure.PasswordResetTokenRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PasswordFindService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MemberPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.password-reset.expiry-minutes:30}")
    private int tokenExpiryMinutes;

    @Value("${app.password-reset.max-attempts-per-hour:3}")
    private int maxAttemptsPerHour;

    @Value("${app.password-reset.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail.from:noreply@example.com}")
    private String fromEmail;

    public PasswordFindResponse requestPasswordReset(PasswordFindRequest request, String clientIp, String userAgent) {
        log.info("비밀번호 재설정 요청 - username: {}, email: {}, ip: {}",
                request.getUsername(), request.getEmail(), clientIp);

        Member member = memberRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND,
                        "사용자명과 이메일이 일치하는 회원을 찾을 수 없습니다."));

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_ACTIVE, "비활성화된 회원입니다.");
        }

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentAttempts = passwordResetTokenRepository.countRecentTokensByMemberId(member.getId(), oneHourAgo);

        if (recentAttempts >= maxAttemptsPerHour) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS,
                    "1시간 내 최대 요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        passwordResetTokenRepository.invalidateAllTokensByMemberId(member.getId());

        String token = generateResetToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .member(member)
                .expiresAt(expiryDate)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink = baseUrl + "/members/password/reset?token=" + token;
        sendPasswordResetEmail(member.getEmail(), member.getUsername(), resetLink);

        savePasswordResetHistory(member, "PASSWORD_RESET_REQUEST", "SUCCESS",
                "비밀번호 재설정 요청", clientIp, userAgent);

        log.info("비밀번호 재설정 토큰 생성 완료 - memberId: {}, token: {}", member.getId(), token);

        return PasswordFindResponse.builder()
                .success(true)
                .message("비밀번호 재설정 링크가 이메일로 전송되었습니다.")
                .email(member.getEmail())
                .expiresAt(expiryDate.toString())
                .build();
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request, String clientIp, String userAgent) {
        log.info("비밀번호 재설정 실행 - token: {}, ip: {}", request.getToken(), clientIp);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN,
                        "유효하지 않은 재설정 토큰입니다."));

        if (!resetToken.isValid()) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN,
                    "만료되었거나 이미 사용된 토큰입니다.");
        }

        Member member = memberRepository.findById(resetToken.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND,
                        "회원 정보를 찾을 수 없습니다."));

        if (!request.isPasswordMatch()) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH,
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        validatePasswordHistory(member.getId(), request.getNewPassword());

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.updatePassword(encodedPassword);
        memberRepository.save(member);

        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        savePasswordHistory(member, encodedPassword, "PASSWORD_RESET", "SUCCESS",
                "비밀번호 재설정으로 변경", clientIp, userAgent);

        savePasswordResetHistory(member, "PASSWORD_RESET_COMPLETE", "SUCCESS",
                "비밀번호 재설정 완료", clientIp, userAgent);

        log.info("비밀번호 재설정 완료 - memberId: {}", member.getId());
    }

    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                System.currentTimeMillis();
    }

    private void sendPasswordResetEmail(String email, String username, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom(fromEmail);
            message.setSubject("[통신판매사업자관리 시스템] 비밀번호 재설정");
            message.setText(buildEmailContent(username, resetLink));

            mailSender.send(message);
            log.info("비밀번호 재설정 이메일 전송 완료 - email: {}", email);
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 전송 실패 - email: {}", email, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "이메일 전송에 실패했습니다.");
        }
    }

    private String buildEmailContent(String username, String resetLink) {
        return String.format("""
                안녕하세요 %s님,

                통신판매사업자관리 시스템에서 비밀번호 재설정을 요청하셨습니다.

                아래 링크를 클릭하여 새 비밀번호를 설정해주세요:
                %s

                ※ 이 링크는 %d분 후에 만료됩니다.
                ※ 본인이 요청하지 않은 경우 이 이메일을 무시하세요.

                감사합니다.
                """, username, resetLink, tokenExpiryMinutes);
    }

    private void validatePasswordHistory(Long memberId, String newPassword) {
        var recentPasswords = passwordHistoryRepository.findRecentPasswordHistoryWithLimit(memberId, 5);

        for (MemberPasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new BusinessException(ErrorCode.RECENTLY_USED_PASSWORD,
                        "최근 사용한 비밀번호와 동일합니다.");
            }
        }
    }

    private void savePasswordHistory(Member member, String passwordHash, String action, String result,
                                     String message, String clientIp, String userAgent) {
        MemberPasswordHistory history = MemberPasswordHistory.builder()
                .member(member)
                .passwordHash(passwordHash)
                .action(action)
                .result(result)
                .message(message)
                .ipAddress(clientIp)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();

        passwordHistoryRepository.save(history);
    }

    private void savePasswordResetHistory(Member member, String action, String result,
                                          String message, String clientIp, String userAgent) {
        MemberPasswordHistory history = MemberPasswordHistory.builder()
                .member(member)
                .action(action)
                .result(result)
                .message(message)
                .ipAddress(clientIp)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();

        passwordHistoryRepository.save(history);
    }
}