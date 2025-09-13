package com.antock.api.member.infrastructure.scheduler;

import com.antock.api.member.infrastructure.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenScheduler {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetTokenScheduler.class);
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = passwordResetTokenRepository.deleteExpiredTokens(now);

            if (deletedCount > 0) {
                log.info("만료된 비밀번호 재설정 토큰 {}개 삭제 완료", deletedCount);
            }
        } catch (Exception e) {
            log.error("만료된 토큰 정리 중 오류 발생", e);
        }
    }
}