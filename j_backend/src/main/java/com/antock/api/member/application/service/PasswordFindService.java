package com.antock.api.member.application.service;

import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
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

        @Value("${app.mail.from:sleekydz86@naver.com}")
        private String fromEmail;

        public PasswordFindResponse requestPasswordReset(PasswordFindRequest request, String clientIp,
                        String userAgent) {
                log.info("비밀번호 재설정 요청 - username: {}, email: {}, ip: {}",
                                request.getUsername(), request.getEmail(), clientIp);

                Member member = memberRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail())
                                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND,
                                                "사용자명과 이메일이 일치하는 회원을 찾을 수 없습니다."));

                if (!member.isActive()) {
                        throw new BusinessException(ErrorCode.MEMBER_NOT_ACTIVE, "비활성화된 회원입니다.");
                }

                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                long recentAttempts = passwordResetTokenRepository.countRecentTokensByMemberId(member.getId(),
                                oneHourAgo);

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
                try {
                        log.info("비밀번호 재설정 시작 - token: {}, ip: {}", request.getToken(), clientIp);
                        log.info("요청된 새 비밀번호 길이: {}",
                                        request.getNewPassword() != null ? request.getNewPassword().length() : "null");
                        log.info("비밀번호 확인 길이: {}",
                                        request.getNewPasswordConfirm() != null
                                                        ? request.getNewPasswordConfirm().length()
                                                        : "null");
                        log.info("비밀번호 일치 여부: {}", request.isPasswordMatch());

                        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                                        .orElseThrow(() -> {
                                                log.error("토큰을 찾을 수 없음 - token: {}", request.getToken());
                                                return new BusinessException(ErrorCode.INVALID_TOKEN,
                                                                "유효하지 않은 재설정 토큰입니다.");
                                        });

                        log.info("토큰 찾음 - used: {}, expired: {}, valid: {}", resetToken.isUsed(),
                                        resetToken.isExpired(), resetToken.isValid());

                        if (!resetToken.isValid()) {
                                log.error("토큰이 유효하지 않음 - used: {}, expired: {}", resetToken.isUsed(),
                                                resetToken.isExpired());
                                throw new BusinessException(ErrorCode.EXPIRED_TOKEN, "만료되었거나 이미 사용된 토큰입니다.");
                        }

                        Member member = memberRepository.findById(resetToken.getMemberId())
                                        .orElseThrow(() -> {
                                                log.error("회원을 찾을 수 없음 - memberId: {}", resetToken.getMemberId());
                                                return new BusinessException(ErrorCode.MEMBER_NOT_FOUND,
                                                                "회원 정보를 찾을 수 없습니다.");
                                        });

                        log.info("회원 찾음 - memberId: {}, username: {}", member.getId(), member.getUsername());

                        if (!request.isPasswordMatch()) {
                                log.error("비밀번호 확인 불일치");
                                throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH,
                                                "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                        }

                        log.info("비밀번호 히스토리 검증 시작");
                        validatePasswordHistory(member.getId(), request.getNewPassword());
                        log.info("비밀번호 히스토리 검증 완료");

                        log.info("비밀번호 인코딩 시작");
                        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
                        log.info("비밀번호 인코딩 완료");

                        log.info("회원 비밀번호 업데이트 시작");
                        member.updatePassword(encodedPassword);
                        log.info("회원 비밀번호 업데이트 완료");

                        log.info("회원 정보 저장 시작");
                        memberRepository.save(member);
                        log.info("회원 정보 저장 완료");

                        log.info("토큰 사용 처리 시작");
                        resetToken.markAsUsed();
                        passwordResetTokenRepository.save(resetToken);
                        log.info("토큰 사용 처리 완료");

                        log.info("비밀번호 히스토리 저장 시작");
                        savePasswordHistory(member, encodedPassword, "PASSWORD_RESET", "SUCCESS", "비밀번호 재설정으로 변경",
                                        clientIp, userAgent);
                        log.info("비밀번호 히스토리 저장 완료");

                        log.info("비밀번호 재설정 히스토리 저장 시작");
                        savePasswordResetHistory(member, "PASSWORD_RESET_COMPLETE", "SUCCESS", "비밀번호 재설정 완료", clientIp,
                                        userAgent);
                        log.info("비밀번호 재설정 히스토리 저장 완료");

                        log.info("비밀번호 재설정 완료 - memberId: {}", member.getId());
                } catch (BusinessException e) {
                        log.error("비밀번호 재설정 실패 (비즈니스 예외) - token: {}, error: {}", request.getToken(), e.getMessage(),
                                        e);
                        throw e;
                } catch (Exception e) {
                        log.error("비밀번호 재설정 실패 (예외) - token: {}", request.getToken(), e);
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                                        "비밀번호 재설정 중 오류가 발생했습니다: " + e.getMessage());
                }
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
                        MimeMessage message = mailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                        helper.setTo(email);
                        helper.setFrom(fromEmail);
                        helper.setSubject("[통신판매사업자관리 시스템] 비밀번호 재설정");
                        helper.setText(buildEmailContent(username, resetLink), true); // HTML 지원

                        mailSender.send(message);
                        log.info("비밀번호 재설정 이메일 전송 완료 - email: {}", email);
                } catch (Exception e) {
                        log.error("비밀번호 재설정 이메일 전송 실패 - email: {}", email, e);
                        throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "이메일 전송에 실패했습니다.");
                }
        }

        private String buildEmailContent(String username, String resetLink) {
                return String.format(
                                """
                                                <!DOCTYPE html>
                                                <html>
                                                <head>
                                                    <meta charset="UTF-8">
                                                    <title>비밀번호 재설정</title>
                                                </head>
                                                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                                                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                                                        <h2 style="color: #2c3e50;">비밀번호 재설정 요청</h2>
                                                        <p>안녕하세요 <strong>%s</strong>님,</p>
                                                        <p>통신판매사업자관리 시스템에서 비밀번호 재설정을 요청하셨습니다.</p>
                                                        <p>아래 버튼을 클릭하여 새 비밀번호를 설정해주세요:</p>
                                                        <div style="text-align: center; margin: 30px 0;">
                                                            <a href="%s" style="background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">비밀번호 재설정</a>
                                                        </div>
                                                        <p style="color: #e74c3c; font-size: 14px;">
                                                            ※ 이 링크는 %d분 후에 만료됩니다.<br>
                                                            ※ 본인이 요청하지 않은 경우 이 이메일을 무시하세요.
                                                        </p>
                                                        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                                                        <p style="font-size: 12px; color: #7f8c8d;">감사합니다.</p>
                                                    </div>
                                                </body>
                                                </html>
                                                """,
                                username, resetLink, tokenExpiryMinutes);
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
                                .passwordHash("N/A")
                                .createdAt(LocalDateTime.now())
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