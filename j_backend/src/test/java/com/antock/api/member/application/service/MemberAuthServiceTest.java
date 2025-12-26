package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberAuthService 테스트")
class MemberAuthServiceTest {

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private RateLimitServiceInterface rateLimitService;

    @Mock
    private MemberCacheService memberCacheService;

    @Mock
    private Executor asyncExecutor;

    @InjectMocks
    private MemberAuthService memberAuthService;

    private Member testMember;
    private MemberLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .username("testuser")
                .password("$2a$10$encoded")
                .nickname("test")
                .email("test@test.com")
                .status(MemberStatus.APPROVED)
                .role(Role.USER)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testMember, "id", 1L);

        loginRequest = MemberLoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(testMember.matchPassword("password123")).thenReturn(true);
        when(authTokenService.generateAccessToken(any(Member.class))).thenReturn("accessToken");
        when(authTokenService.generateRefreshToken(any(Member.class))).thenReturn("refreshToken");
        when(memberDomainService.save(any(Member.class))).thenReturn(testMember);

        MemberLoginResponse response = memberAuthService.login(loginRequest, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        verify(rateLimitService).checkRateLimit("127.0.0.1", "login");
        verify(memberDomainService).save(any(Member.class));
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_userNotFound() {
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberAuthService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_passwordMismatch() {
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(testMember.matchPassword("password123")).thenReturn(false);
        when(testMember.getLoginFailCount()).thenReturn(0);

        assertThatThrownBy(() -> memberAuthService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인 실패 - 거부된 회원")
    void login_rejectedMember() {
        testMember = Member.builder()
                .username("testuser")
                .password("$2a$10$encoded")
                .status(MemberStatus.REJECTED)
                .build();
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> memberAuthService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_APPROVED);
    }

    @Test
    @DisplayName("로그인 실패 - 정지된 회원")
    void login_suspendedMember() {
        testMember = Member.builder()
                .username("testuser")
                .password("$2a$10$encoded")
                .status(MemberStatus.SUSPENDED)
                .build();
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> memberAuthService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_APPROVED);
    }

    @Test
    @DisplayName("로그인 실패 - 승인 대기중인 회원")
    void login_pendingMember() {
        testMember = Member.builder()
                .username("testuser")
                .password("$2a$10$encoded")
                .status(MemberStatus.PENDING)
                .build();
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> memberAuthService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_APPROVED);
    }

    @Test
    @DisplayName("로그인 실패 - 계정 잠김")
    void login_lockedAccount() {
        testMember = Member.builder()
                .username("testuser")
                .password("$2a$10$encoded")
                .status(MemberStatus.APPROVED)
                .build();
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.of(testMember));
        when(testMember.isLocked()).thenReturn(true);

        assertThatThrownBy(() -> memberAuthService.login(loginRequest, "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_LOCKED);
    }
}
