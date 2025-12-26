package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.domain.Member;
import com.antock.api.member.domain.MemberPasswordHistory;
import com.antock.api.member.infrastructure.MemberPasswordHistoryRepository;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberPasswordService 테스트")
class MemberPasswordServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberPasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberPasswordService memberPasswordService;

    private Member testMember;
    private MemberPasswordChangeRequest changeRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .username("testuser")
                .password("$2a$10$oldEncodedPassword")
                .email("test@test.com")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testMember, "id", 1L);

        changeRequest = MemberPasswordChangeRequest.builder()
                .oldPassword("oldPassword")
                .newPassword("NewPassword123!")
                .newPasswordConfirm("NewPassword123!")
                .build();
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);
        when(passwordHistoryRepository.countPasswordChangesAfter(anyLong(), any(LocalDateTime.class))).thenReturn(0L);
        when(passwordHistoryRepository.findRecentPasswordHistoryWithLimit(anyLong(), anyInt()))
                .thenReturn(new ArrayList<>());
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("$2a$10$newEncodedPassword");
        when(passwordHistoryRepository.save(any(MemberPasswordHistory.class))).thenReturn(new MemberPasswordHistory());
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        memberPasswordService.changePassword(1L, changeRequest);

        verify(passwordHistoryRepository).save(any(MemberPasswordHistory.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 회원 없음")
    void changePassword_memberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberPasswordService.changePassword(1L, changeRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호 불일치")
    void changePassword_oldPasswordMismatch() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> memberPasswordService.changePassword(1L, changeRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OLD_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 일일 변경 횟수 초과")
    void changePassword_dailyLimitExceeded() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);
        when(passwordHistoryRepository.countPasswordChangesAfter(anyLong(), any(LocalDateTime.class))).thenReturn(3L);

        assertThatThrownBy(() -> memberPasswordService.changePassword(1L, changeRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_PASSWORD_CHANGE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 비밀번호 확인 불일치")
    void changePassword_passwordConfirmationMismatch() {
        changeRequest = MemberPasswordChangeRequest.builder()
                .oldPassword("oldPassword")
                .newPassword("NewPassword123!")
                .newPasswordConfirm("DifferentPassword123!")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> memberPasswordService.changePassword(1L, changeRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 약한 비밀번호")
    void changePassword_weakPassword() {
        changeRequest = MemberPasswordChangeRequest.builder()
                .oldPassword("oldPassword")
                .newPassword("weak")
                .newPasswordConfirm("weak")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> memberPasswordService.changePassword(1L, changeRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WEAK_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 최근 사용한 비밀번호")
    void changePassword_recentlyUsedPassword() {
        MemberPasswordHistory history = MemberPasswordHistory.builder()
                .passwordHash("$2a$10$recentPassword")
                .build();
        List<MemberPasswordHistory> histories = List.of(history);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches("oldPassword", "$2a$10$oldEncodedPassword")).thenReturn(true);
        when(passwordHistoryRepository.countPasswordChangesAfter(anyLong(), any(LocalDateTime.class))).thenReturn(0L);
        when(passwordHistoryRepository.findRecentPasswordHistoryWithLimit(anyLong(), anyInt())).thenReturn(histories);
        when(passwordEncoder.matches("NewPassword123!", "$2a$10$recentPassword")).thenReturn(true);

        assertThatThrownBy(() -> memberPasswordService.changePassword(1L, changeRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECENTLY_USED_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 필요 여부 확인")
    void isPasswordChangeRequired() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(testMember.isPasswordChangeRequired()).thenReturn(true);

        boolean result = memberPasswordService.isPasswordChangeRequired(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 권장 여부 확인")
    void isPasswordChangeRecommended() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(testMember.isPasswordChangeRecommended()).thenReturn(false);

        boolean result = memberPasswordService.isPasswordChangeRecommended(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("오늘 비밀번호 변경 횟수 조회")
    void getTodayPasswordChangeCount() {
        when(passwordHistoryRepository.countPasswordChangesAfter(anyLong(), any(LocalDateTime.class))).thenReturn(2L);

        long count = memberPasswordService.getTodayPasswordChangeCount(1L);

        assertThat(count).isEqualTo(2L);
    }
}
