package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Member 서비스 통합 테스트")
class MemberServiceIntegrationTest {

    @Autowired
    private MemberManagementService memberManagementService;

    @Autowired
    private MemberAuthService memberAuthService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MemberJoinRequest joinRequest;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        joinRequest = MemberJoinRequest.builder()
                .username("testuser")
                .password("Password123!")
                .email("test@test.com")
                .nickname("test")
                .build();
    }

    @Test
    @DisplayName("회원가입 후 로그인 통합 테스트")
    void joinAndLogin_integration() {
        var joinResponse = memberManagementService.join(joinRequest);
        assertThat(joinResponse).isNotNull();
        assertThat(joinResponse.getUsername()).isEqualTo("testuser");

        Member member = memberRepository.findByUsername("testuser").orElseThrow();
        member.approve(member.getId());
        memberRepository.save(member);

        MemberLoginRequest loginRequest = MemberLoginRequest.builder()
                .username("testuser")
                .password("Password123!")
                .build();

        var loginResponse = memberAuthService.login(loginRequest, "127.0.0.1");
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getAccessToken()).isNotNull();
    }

    @Test
    @DisplayName("중복 회원가입 실패 통합 테스트")
    void duplicateJoin_integration() {
        memberManagementService.join(joinRequest);

        assertThatThrownBy(() -> memberManagementService.join(joinRequest))
                .isInstanceOf(BusinessException.class);
    }
}

