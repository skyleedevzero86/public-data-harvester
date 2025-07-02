package com.antock.member.application.service;

import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.AuthTokenService;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.MemberCacheService;
import com.antock.api.member.application.service.MemberDomainService;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberApplicationServiceTest {

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private RateLimitServiceInterface rateLimitService;

    @Mock
    private MemberCacheService memberCacheService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MemberApplicationService memberApplicationService;

    @BeforeEach
    void setUp() {
        memberApplicationService = new MemberApplicationService(
                memberDomainService,
                authTokenService,
                rateLimitService,
                memberCacheService,
                passwordEncoder
        );
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 - 캐시 히트")
    void getCurrentMemberInfo_CacheHit() {
        // given
        Long memberId = 1L;
        MemberResponse cachedResponse = createTestMemberResponse(memberId);

        when(memberCacheService.getMemberFromCache(memberId)).thenReturn(cachedResponse);

        // when
        MemberResponse result = memberApplicationService.getCurrentMemberInfo(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);

        verify(memberCacheService).getMemberFromCache(memberId);
        verify(memberDomainService, never()).findById(any());
        verify(memberCacheService, never()).cacheMemberResponse(any());
    }

    @Test
    @DisplayName("현재 사용자 정보 조회 - 캐시 미스 후 DB 조회")
    void getCurrentMemberInfo_CacheMiss_ThenDbQuery() {
        // given
        Long memberId = 1L;
        Member testMember = createTestMember(memberId);

        when(memberCacheService.getMemberFromCache(memberId)).thenReturn(null);
        when(memberDomainService.findById(memberId)).thenReturn(Optional.of(testMember));

        // when
        MemberResponse result = memberApplicationService.getCurrentMemberInfo(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(memberId);

        verify(memberCacheService).getMemberFromCache(memberId);
        verify(memberDomainService).findById(memberId);
        verify(memberCacheService).cacheMemberResponse(any(MemberResponse.class));
    }

    @Test
    @DisplayName("프로필 업데이트 시 캐시 무효화")
    void updateProfile_EvictsCache() {
        // given
        Long memberId = 1L;
        MemberUpdateRequest request = createMemberUpdateRequest("New Nickname", "new@example.com");
        Member updatedMember = createTestMember(memberId);

        when(memberDomainService.updateMemberProfile(memberId, "New Nickname", "new@example.com"))
                .thenReturn(updatedMember);

        // when
        MemberResponse result = memberApplicationService.updateProfile(memberId, request);

        // then
        assertThat(result).isNotNull();

        verify(memberCacheService).evictMemberCache(memberId);
        verify(memberCacheService).cacheMemberResponse(any(MemberResponse.class));
        verify(memberCacheService).cacheMemberProfile(any(MemberResponse.class));
    }

    @Test
    @DisplayName("회원 승인 시 캐시 무효화")
    void approveMember_EvictsCache() {
        // given
        Long memberId = 1L;
        Long approverId = 2L;
        Member approvedMember = createTestMember(memberId);

        when(memberDomainService.approveMember(memberId, approverId)).thenReturn(approvedMember);

        // when
        MemberResponse result = memberApplicationService.approveMember(memberId, approverId);

        // then
        assertThat(result).isNotNull();

        verify(memberCacheService).evictMemberCache(memberId);
        verify(memberCacheService).cacheMemberResponse(any(MemberResponse.class));
    }

    @Test
    @DisplayName("캐시 통계 조회")
    void getCacheStatistics_Success() {
        // given
        MemberCacheService.CacheStatistics expectedStats =
                new MemberCacheService.CacheStatistics(10, 2, 0, 83.3, 12, true);

        when(memberCacheService.getCacheStatistics()).thenReturn(expectedStats);

        // when
        MemberCacheService.CacheStatistics result = memberApplicationService.getCacheStatistics();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCacheHits()).isEqualTo(10);
        assertThat(result.getCacheMisses()).isEqualTo(2);
        assertThat(result.isCacheAvailable()).isTrue();

        verify(memberCacheService).getCacheStatistics();
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