package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.PasswordFindRequest;
import com.antock.api.member.application.dto.request.PasswordResetRequest;
import com.antock.api.member.application.dto.response.PasswordFindResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.domain.PasswordResetToken;
import com.antock.api.member.infrastructure.MemberPasswordHistoryRepository;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.infrastructure.PasswordResetTokenRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordFindService 테스트")
class PasswordFindServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private MemberPasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private PasswordFindService passwordFindService;

    private Member testMember;
    private PasswordFindRequest findRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordFindService, "tokenExpiryMinutes", 30);
        ReflectionTestUtils.setField(passwordFindService, "maxAttemptsPerHour", 3);
        ReflectionTestUtils.setField(passwordFindService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(passwordFindService, "fromEmail", "test@test.com");

        testMember = Member.builder()
                .username("testuser")
                .email("test@test.com")
                .status(MemberStatus.APPROVED)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testMember, "id", 1L);

        findRequest = PasswordFindRequest.builder()
                .username("testuser")
                .email("test@test.com")
                .build();
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 성공")
    void requestPasswordReset_success() throws Exception {
        when(memberRepository.findByUsernameAndEmail("testuser", "test@test.com")).thenReturn(Optional.of(testMember));
        when(testMember.isActive()).thenReturn(true);
        when(passwordResetTokenRepository.countRecentTokensByMemberId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        when(passwordHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PasswordFindResponse response = passwordFindService.requestPasswordReset(findRequest, "127.0.0.1",
                "test-agent");

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 실패 - 회원 없음")
    void requestPasswordReset_memberNotFound() {
        when(memberRepository.findByUsernameAndEmail("testuser", "test@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordFindService.requestPasswordReset(findRequest, "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 실패 - 비활성화된 회원")
    void requestPasswordReset_inactiveMember() {
        when(memberRepository.findByUsernameAndEmail("testuser", "test@test.com")).thenReturn(Optional.of(testMember));
        when(testMember.isActive()).thenReturn(false);

        assertThatThrownBy(() -> passwordFindService.requestPasswordReset(findRequest, "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_ACTIVE);
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 실패 - 요청 횟수 초과")
    void requestPasswordReset_tooManyRequests() {
        when(memberRepository.findByUsernameAndEmail("testuser", "test@test.com")).thenReturn(Optional.of(testMember));
        when(testMember.isActive()).thenReturn(true);
        when(passwordResetTokenRepository.countRecentTokensByMemberId(anyLong(), any(LocalDateTime.class)))
                .thenReturn(3L);

        assertThatThrownBy(() -> passwordFindService.requestPasswordReset(findRequest, "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword_success() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("validToken")
                .member(testMember)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(token, "used", false);

        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
                .token("validToken")
                .newPassword("NewPassword123!")
                .newPasswordConfirm("NewPassword123!")
                .build();

        when(passwordResetTokenRepository.findByToken("validToken")).thenReturn(Optional.of(token));
        when(token.isValid()).thenReturn(true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordHistoryRepository.findRecentPasswordHistoryWithLimit(anyLong(), anyInt()))
                .thenReturn(new ArrayList<>());
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("$2a$10$encoded");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(token);
        when(passwordHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        passwordFindService.resetPassword(resetRequest, "127.0.0.1", "test-agent");

        verify(memberRepository).save(any(Member.class));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 토큰 없음")
    void resetPassword_tokenNotFound() {
        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
                .token("invalidToken")
                .newPassword("NewPassword123!")
                .newPasswordConfirm("NewPassword123!")
                .build();

        when(passwordResetTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordFindService.resetPassword(resetRequest, "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 만료된 토큰")
    void resetPassword_expiredToken() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("expiredToken")
                .member(testMember)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
                .token("expiredToken")
                .newPassword("NewPassword123!")
                .newPasswordConfirm("NewPassword123!")
                .build();

        when(passwordResetTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(token));
        when(token.isValid()).thenReturn(false);

        assertThatThrownBy(() -> passwordFindService.resetPassword(resetRequest, "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 비밀번호 확인 불일치")
    void resetPassword_passwordMismatch() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("validToken")
                .member(testMember)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        PasswordResetRequest resetRequest = PasswordResetRequest.builder()
                .token("validToken")
                .newPassword("NewPassword123!")
                .newPasswordConfirm("DifferentPassword123!")
                .build();

        when(passwordResetTokenRepository.findByToken("validToken")).thenReturn(Optional.of(token));
        when(token.isValid()).thenReturn(true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> passwordFindService.resetPassword(resetRequest, "127.0.0.1", "test-agent"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
    }

    @Test
    @DisplayName("토큰 유효성 검증")
    void validateResetToken() {
        PasswordResetToken token = PasswordResetToken.builder()
                .token("validToken")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(passwordResetTokenRepository.findByToken("validToken")).thenReturn(Optional.of(token));
        when(token.isValid()).thenReturn(true);

        boolean isValid = passwordFindService.validateResetToken("validToken");

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 유효성 검증 실패")
    void validateResetToken_invalid() {
        when(passwordResetTokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

        boolean isValid = passwordFindService.validateResetToken("invalidToken");

        assertThat(isValid).isFalse();
    }
}
