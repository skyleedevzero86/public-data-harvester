package com.antock.api.member.infrastructure;

import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MemberRepository 통합 테스트")
class MemberRepositoryIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 저장 및 조회")
    void saveAndFind() {
        Member member = Member.builder()
                .username("testuser")
                .password("encoded")
                .nickname("test")
                .email("test@test.com")
                .status(MemberStatus.PENDING)
                .role(Role.USER)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        Member saved = memberRepository.save(member);
        Member found = memberRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("사용자명으로 조회")
    void findByUsername() {
        Member member = Member.builder()
                .username("testuser")
                .password("encoded")
                .nickname("test")
                .email("test@test.com")
                .status(MemberStatus.APPROVED)
                .role(Role.USER)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        memberRepository.save(member);

        var found = memberRepository.findByUsername("testuser");

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("상태별 회원 조회")
    void findByStatus() {
        Member member = Member.builder()
                .username("testuser")
                .password("encoded")
                .nickname("test")
                .email("test@test.com")
                .status(MemberStatus.PENDING)
                .role(Role.USER)
                .passwordChangedAt(LocalDateTime.now())
                .build();

        memberRepository.save(member);

        Page<Member> page = memberRepository.findByStatus(
                MemberStatus.PENDING, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
    }
}

