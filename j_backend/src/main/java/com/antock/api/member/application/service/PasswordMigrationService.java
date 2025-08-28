package com.antock.api.member.application.service;

import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordMigrationService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void migratePlainTextPasswords() {
        log.info("평문 비밀번호 마이그레이션 시작");

        List<Member> members = memberRepository.findAll();
        int migratedCount = 0;

        for (Member member : members) {
            String currentPassword = member.getPassword();

            if (isEncryptedPassword(currentPassword)) {
                log.debug("이미 암호화된 비밀번호입니다. memberId: {}, username: {}",
                        member.getId(), member.getUsername());
                continue;
            }

            try {
                String encodedPassword = passwordEncoder.encode(currentPassword);
                member.changePassword(encodedPassword);

                memberRepository.save(member);
                migratedCount++;

                log.info("비밀번호 마이그레이션 완료: memberId: {}, username: {}",
                        member.getId(), member.getUsername());

            } catch (Exception e) {
                log.error("비밀번호 마이그레이션 실패: memberId: {}, username: {}, error: {}",
                        member.getId(), member.getUsername(), e.getMessage());
            }
        }

        log.info("평문 비밀번호 마이그레이션 완료: 총 {}개 중 {}개 마이그레이션됨",
                members.size(), migratedCount);
    }

    private boolean isEncryptedPassword(String password) {
        return password != null &&
                (password.startsWith("$2a$") ||
                        password.startsWith("$2b$") ||
                        password.startsWith("$2y$"));
    }
}