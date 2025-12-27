package com.antock.member.application.service;

import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.*;
import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberApplicationServiceTest {

    @Mock
    private MemberAuthService memberAuthService;

    @Mock
    private MemberManagementService memberManagementService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private MemberPasswordService memberPasswordService;

    @Mock
    private MemberCacheService memberCacheService;

    private MemberApplicationService memberApplicationService;

    @BeforeEach
    void setUp() {
        memberApplicationService = new MemberApplicationService(
                memberAuthService,
                memberManagementService,
                memberQueryService,
                memberPasswordService,
                memberCacheService
        );
    }

    @Test
    @DisplayName("사용자 정보 조회")
    void getMemberInfo_Success() {
        Long memberId = 1L;
        MemberResponse expectedResponse = createTestMemberResponse(memberId);

        when(memberQueryService.getMemberInfo(memberId)).thenReturn(expectedResponse);

        MemberResponse result = memberApplicationService.getMemberInfo(memberId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);

        verify(memberQueryService).getMemberInfo(memberId);
    }

    @Test
    @DisplayName("사용자 정보 조회 - 캐시 사용")
    void getMemberInfo_WithCache() {
        Long memberId = 1L;
        MemberResponse expectedResponse = createTestMemberResponse(memberId);

        when(memberQueryService.getMemberInfo(memberId)).thenReturn(expectedResponse);

        MemberResponse result = memberApplicationService.getMemberInfo(memberId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);

        verify(memberQueryService).getMemberInfo(memberId);
    }

    @Test
    @DisplayName("프로필 업데이트")
    void updateProfile_Success() {
        Long memberId = 1L;
        MemberUpdateRequest request = createMemberUpdateRequest("New Nickname", "new@example.com");
        MemberResponse expectedResponse = createTestMemberResponse(memberId);

        when(memberManagementService.updateProfile(memberId, request)).thenReturn(expectedResponse);

        MemberResponse result = memberApplicationService.updateProfile(memberId, request);

        assertThat(result).isNotNull();
        verify(memberManagementService).updateProfile(memberId, request);
    }

    @Test
    @DisplayName("회원 승인")
    void approveMember_Success() {
        Long memberId = 1L;
        Long approverId = 2L;
        MemberResponse expectedResponse = createTestMemberResponse(memberId);

        when(memberManagementService.approveMember(memberId, approverId)).thenReturn(expectedResponse);

        MemberResponse result = memberApplicationService.approveMember(memberId, approverId);

        assertThat(result).isNotNull();
        verify(memberManagementService).approveMember(memberId, approverId);
    }

    @Test
    @DisplayName("캐시 통계 조회")
    void getCacheStatistics_Success() {
        MemberCacheService.CacheStatistics expectedStats =
                new MemberCacheService.CacheStatistics(10, 2, 0, 83.3, 12, true);

        when(memberCacheService.getCacheStatistics()).thenReturn(expectedStats);

        MemberCacheService.CacheStatistics result = memberApplicationService.getCacheStatistics();

        assertThat(result).isNotNull();
        assertThat(result.getCacheHits()).isEqualTo(10);
        assertThat(result.getCacheMisses()).isEqualTo(2);
        assertThat(result.isCacheAvailable()).isTrue();

        verify(memberCacheService).getCacheStatistics();
    }

    @Test
    @DisplayName("캐시 서비스가 null인 경우 빈 통계 반환")
    void getCacheStatistics_WhenCacheServiceIsNull() {
        memberApplicationService = new MemberApplicationService(
                memberAuthService,
                memberManagementService,
                memberQueryService,
                memberPasswordService,
                null
        );

        MemberCacheService.CacheStatistics result = memberApplicationService.getCacheStatistics();

        assertThat(result).isNotNull();
        assertThat(result.getCacheHits()).isEqualTo(0);
        assertThat(result.getCacheMisses()).isEqualTo(0);
        assertThat(result.isCacheAvailable()).isFalse();
    }

    private Member createTestMember(Long memberId) {
        Member member = Member.builder()
                .username("testuser")
                .password("encoded-password")
                .nickname("Test User")
                .email("test@example.com")
                .apiKey("test-api-key-" + memberId)
                .status(MemberStatus.APPROVED)
                .role(Role.USER)
                .loginFailCount(0)
                .build();

        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "createDate", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(member, "modifyDate", LocalDateTime.now());

        return member;
    }

    private MemberResponse createTestMemberResponse(Long memberId) {
        Member member = createTestMember(memberId);
        return MemberResponse.from(member);
    }

    private MemberUpdateRequest createMemberUpdateRequest(String nickname, String email) {
        return MemberUpdateRequest.builder()
                .nickname(nickname)
                .email(email)
                .build();
    }
}