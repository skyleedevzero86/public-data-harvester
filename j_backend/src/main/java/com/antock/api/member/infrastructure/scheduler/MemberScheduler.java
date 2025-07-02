package com.antock.api.member.infrastructure.scheduler;

import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberScheduler {

    private final MemberRepository memberRepository;

    /**
     * 매일 자정에 잠긴 계정 해제 처리
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void unlockExpiredAccounts() {
        log.info("계정 잠금 해제 스케줄러 시작");

        LocalDateTime unlockTime = LocalDateTime.now().minusHours(24);
        List<Member> lockedMembers = memberRepository.findLockedMembersBeforeUnlockTime(unlockTime);

        for (Member member : lockedMembers) {
            member.resetLoginFailCount();
            log.info("계정 잠금 해제: 사용자 ID = {}, 사용자명 = {}", member.getId(), member.getUsername());
        }

        memberRepository.saveAll(lockedMembers);
        log.info("계정 잠금 해제 완료: {} 개 계정", lockedMembers.size());
    }

    /**
     * 매주 월요일 오전 9시에 승인 대기 회원 알림
     */
    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional(readOnly = true)
    public void notifyPendingMembers() {
        log.info("승인 대기 회원 알림 스케줄러 시작");

        List<Member> pendingMembers = memberRepository.findPendingMembersAfter(
                com.antock.domain.member.vo.MemberStatus.PENDING,
                LocalDateTime.now().minusDays(7)
        );

        if (!pendingMembers.isEmpty()) {
            log.warn("승인 대기중인 회원 {}명이 있습니다.", pendingMembers.size());
            // 여기서 관리자에게 알림 발송 로직 추가 예정
        }
    }
}
