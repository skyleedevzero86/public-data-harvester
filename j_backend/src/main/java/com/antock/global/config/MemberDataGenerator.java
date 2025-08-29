package com.antock.global.config;

import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDataGenerator {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataInitProperties dataInitProperties;

    private static final int ADMIN_RATIO = 5;
    private static final int MANAGER_RATIO = 15;
    private static final int USER_RATIO = 80;

    private static final int APPROVED_RATIO = 70;
    private static final int PENDING_RATIO = 15;
    private static final int SUSPENDED_RATIO = 10;
    private static final int REJECTED_RATIO = 3;
    private static final int WITHDRAWN_RATIO = 2;

    @Transactional
    public void generateMembers(int totalCount) {
        if (memberRepository.count() > 0 && !dataInitProperties.isForceInit()) {
            log.info("기존 회원 데이터가 존재합니다. 생성을 건너뜁니다. (강제초기화: {})",
                    dataInitProperties.isForceInit());
            return;
        }

        if (dataInitProperties.isForceInit() && memberRepository.count() > 0) {
            log.warn(" 강제 초기화 모드: 기존 회원 데이터가 있지만 새로운 데이터를 생성합니다.");
        }

        log.info("=== {} 명의 회원 더미 데이터 생성 시작 ===", totalCount);
        long startTime = System.currentTimeMillis();

        int adminCount = totalCount * ADMIN_RATIO / 100;
        int managerCount = totalCount * MANAGER_RATIO / 100;
        int userCount = totalCount - adminCount - managerCount;

        List<Member> allMembers = new ArrayList<>();

        allMembers.addAll(generateMembersByRole(Role.ADMIN, adminCount, "admin"));
        allMembers.addAll(generateMembersByRole(Role.MANAGER, managerCount, "manager"));
        allMembers.addAll(generateMembersByRole(Role.USER, userCount, "user"));

        saveMembersInBatches(allMembers, dataInitProperties.getBatchSize());

        long endTime = System.currentTimeMillis();
        log.info("=== 회원 더미 데이터 생성 완료 ===");
        log.info("총 생성 시간: {}ms", (endTime - startTime));
        log.info("실제 생성된 회원 수: {}", memberRepository.count());

        printGenerationStatistics();
    }

    private List<Member> generateMembersByRole(Role role, int totalCount, String usernamePrefix) {
        List<Member> members = new ArrayList<>();

        int approvedCount = totalCount * APPROVED_RATIO / 100;
        int pendingCount = totalCount * PENDING_RATIO / 100;
        int suspendedCount = totalCount * SUSPENDED_RATIO / 100;
        int rejectedCount = totalCount * REJECTED_RATIO / 100;
        int withdrawnCount = totalCount - approvedCount - pendingCount - suspendedCount - rejectedCount;

        int counter = 1;

        members.addAll(createMembers(role, MemberStatus.APPROVED, approvedCount, usernamePrefix, counter));
        counter += approvedCount;

        members.addAll(createMembers(role, MemberStatus.PENDING, pendingCount, usernamePrefix, counter));
        counter += pendingCount;

        members.addAll(createMembers(role, MemberStatus.SUSPENDED, suspendedCount, usernamePrefix, counter));
        counter += suspendedCount;

        members.addAll(createMembers(role, MemberStatus.REJECTED, rejectedCount, usernamePrefix, counter));
        counter += rejectedCount;

        members.addAll(createMembers(role, MemberStatus.WITHDRAWN, withdrawnCount, usernamePrefix, counter));

        log.info("{} 역할 회원 생성 완료: {} 명", role.getDescription(), members.size());
        return members;
    }

    private List<Member> createMembers(Role role, MemberStatus status, int count, String usernamePrefix, int startNumber) {
        List<Member> members = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int memberNumber = startNumber + i;

            Member member = Member.builder()
                    .username(String.format("%s%04d", usernamePrefix, memberNumber))
                    .password(encodePassword(getPasswordByRole(role)))
                    .nickname(String.format("%s%04d", getRoleKoreanName(role), memberNumber))
                    .email(String.format("%s%04d@example.com", usernamePrefix, memberNumber))
                    .apiKey(generateApiKey())
                    .status(status)
                    .role(role)
                    .passwordChangedAt(generateRandomPastDateTime())
                    .passwordChangeCount(ThreadLocalRandom.current().nextInt(0, 3))
                    .lastPasswordChangeDate(generateRandomPastDate())
                    .build();

            if (status == MemberStatus.APPROVED) {
                setApprovalFields(member);
            }

            if (status == MemberStatus.SUSPENDED) {
                setLockFields(member);
            }

            members.add(member);
        }

        return members;
    }

    private void saveMembersInBatches(List<Member> members, int batchSize) {
        for (int i = 0; i < members.size(); i += batchSize) {
            int end = Math.min(i + batchSize, members.size());
            List<Member> batch = members.subList(i, end);
            memberRepository.saveAll(batch);
            log.info("배치 저장 진행: {}/{} ({:.1f}%)",
                    end, members.size(), (double) end / members.size() * 100);
        }
    }

    private void printGenerationStatistics() {
        log.info("\n === 생성된 회원 통계 ===");

        for (Role role : Role.values()) {
            long roleCount = memberRepository.countByRole(role);
            log.info("👤 {}: {} 명", role.getDescription(), roleCount);

            for (MemberStatus status : MemberStatus.values()) {
                long statusCount = memberRepository.countByRoleAndStatus(role, status);
                if (statusCount > 0) {
                    log.info("   └─ {}: {} 명", status.getDescription(), statusCount);
                }
            }
        }

        log.info("\n === 전체 상태별 통계 ===");
        for (MemberStatus status : MemberStatus.values()) {
            long count = memberRepository.countByStatus(status);
            log.info(" {}: {} 명", status.getDescription(), count);
        }

        log.info("\n 총 생성된 회원 수: {} 명", memberRepository.count());
    }

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private String getPasswordByRole(Role role) {
        return switch (role) {
            case ADMIN -> "Admin@123!";
            case MANAGER -> "Manager@123!";
            case USER -> "User@123!";
        };
    }

    private String getRoleKoreanName(Role role) {
        return switch (role) {
            case ADMIN -> "관리자";
            case MANAGER -> "매니저";
            case USER -> "사용자";
        };
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private LocalDateTime generateRandomPastDateTime() {
        int daysAgo = ThreadLocalRandom.current().nextInt(1, 365);
        int hoursAgo = ThreadLocalRandom.current().nextInt(0, 24);
        int minutesAgo = ThreadLocalRandom.current().nextInt(0, 60);

        return LocalDateTime.now()
                .minusDays(daysAgo)
                .minusHours(hoursAgo)
                .minusMinutes(minutesAgo);
    }

    private LocalDate generateRandomPastDate() {
        int daysAgo = ThreadLocalRandom.current().nextInt(1, 90);
        return LocalDate.now().minusDays(daysAgo);
    }

    private void setApprovalFields(Member member) {
        try {
            var approvedByField = Member.class.getDeclaredField("approvedBy");
            approvedByField.setAccessible(true);
            approvedByField.set(member, 1L);

            var approvedAtField = Member.class.getDeclaredField("approvedAt");
            approvedAtField.setAccessible(true);
            approvedAtField.set(member, generateRandomPastDateTime());
        } catch (Exception e) {
            log.debug("승인 정보 설정 중 오류 (무시됨): {}", e.getMessage());
        }
    }

    private void setLockFields(Member member) {
        try {
            var loginFailCountField = Member.class.getDeclaredField("loginFailCount");
            loginFailCountField.setAccessible(true);
            loginFailCountField.set(member, 5);

            var accountLockedAtField = Member.class.getDeclaredField("accountLockedAt");
            accountLockedAtField.setAccessible(true);
            accountLockedAtField.set(member, generateRandomPastDateTime());
        } catch (Exception e) {
            log.debug("잠금 정보 설정 중 오류 (무시됨): {}", e.getMessage());
        }
    }
}