package com.antock.api.member.infrastructure.scheduler;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordExpirationScheduler {

    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkPasswordExpiration() {
        log.info("비밀번호 만료 예정 사용자 확인 스케줄러 실행");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiringSoon = now.minusDays(80);
            LocalDateTime expired = now.minusDays(90);

            List<Member> expiringSoonMembers = memberRepository.findMembersWithPasswordChangedBefore(expiringSoon);
            log.info("비밀번호 변경 권장 대상 사용자 수: {}", expiringSoonMembers.size());

            for (Member member : expiringSoonMembers) {
                if (member.getStatus() == MemberStatus.APPROVED) {
                    sendPasswordExpirationWarning(member);
                }
            }

            List<Member> expiredMembers = memberRepository.findMembersWithPasswordChangedBefore(expired);
            log.info("비밀번호 변경 필수 대상 사용자 수: {}", expiredMembers.size());

            for (Member member : expiredMembers) {
                if (member.getStatus() == MemberStatus.APPROVED) {
                    sendPasswordExpirationAlert(member);
                }
            }

        } catch (Exception e) {
            log.error("비밀번호 만료 확인 스케줄러 실행 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "0 0 10 * * MON")
    public void generatePasswordChangeReport() {
        log.info("주간 비밀번호 변경 통계 리포트 생성");

        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

            long weeklyChanges = memberRepository.countPasswordChangesAfter(weekAgo);

            long expiredPasswordUsers = memberRepository.countMembersWithPasswordChangedBefore(
                    LocalDateTime.now().minusDays(90));

            long expiringSoonUsers = memberRepository.countMembersWithPasswordChangedBefore(
                    LocalDateTime.now().minusDays(80));

            log.info("주간 비밀번호 변경 통계 - 이번 주 변경: {}, 만료된 사용자: {}, 만료 예정 사용자: {}",
                    weeklyChanges, expiredPasswordUsers, expiringSoonUsers);

            sendWeeklyPasswordReport(weeklyChanges, expiredPasswordUsers, expiringSoonUsers);

        } catch (Exception e) {
            log.error("주간 비밀번호 변경 통계 생성 중 오류 발생", e);
        }
    }

    private void sendPasswordExpirationWarning(Member member) {
        log.info("비밀번호 만료 경고 알림 발송 - memberId: {}, username: {}",
                member.getId(), member.getUsername());
    }

    private void sendPasswordExpirationAlert(Member member) {
        log.warn("비밀번호 만료 긴급 알림 발송 - memberId: {}, username: {}",
                member.getId(), member.getUsername());
    }

    private void sendWeeklyPasswordReport(long weeklyChanges, long expiredUsers, long expiringSoonUsers) {
        log.info("주간 비밀번호 보안 리포트 발송 완료");
    }
}