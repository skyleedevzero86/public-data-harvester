package com.antock.member.application.service;

import com.antock.api.member.application.service.MemberDomainService;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberDomainServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberDomainService memberDomainService;

    @Test
    @DisplayName("회원 생성 시 성공")
    void createMember_Success() {
        // given
        String username = "testuser";
        String password = "encoded_password";
        String nickname = "테스트";
        String email = "test@example.com";

        given(memberRepository.existsByUsername(username)).willReturn(false);
        given(memberRepository.existsByEmail(email)).willReturn(false);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            return member;
        });

        // when
        Member result = memberDomainService.createMember(username, password, nickname, email);

        // then
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getStatus()).isEqualTo(MemberStatus.PENDING);
        assertThat(result.getRole()).isEqualTo(Role.USER);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("중복된 사용자명으로 회원 생성 시 예외 발생")
    void createMember_DuplicateUsername_ThrowsException() {
        // given
        String username = "testuser";
        given(memberRepository.existsByUsername(username)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberDomainService.createMember(username, "password", "nickname", "email@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("회원 승인 성공")
    void approveMember_Success() {
        // given
        Long memberId = 1L;
        Long approverId = 2L;

        Member member = Member.builder()
                .username("testuser")
                .password("password")
                .nickname("테스트")
                .email("test@example.com")
                .status(MemberStatus.PENDING)
                .build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Member result = memberDomainService.approveMember(memberId, approverId);

        // then
        assertThat(result.getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(result.getApprovedBy()).isEqualTo(approverId);
        assertThat(result.getApprovedAt()).isNotNull();
    }
}
