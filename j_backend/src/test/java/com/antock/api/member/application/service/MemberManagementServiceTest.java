package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberManagementService 테스트")
class MemberManagementServiceTest {

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private MemberCacheService memberCacheService;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberManagementService memberManagementService;

    private Member testMember;
    private MemberJoinRequest joinRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .username("testuser")
                .email("test@test.com")
                .nickname("test")
                .status(MemberStatus.APPROVED)
                .role(Role.USER)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testMember, "id", 1L);

        joinRequest = MemberJoinRequest.builder()
                .username("newuser")
                .password("Password123!")
                .email("new@test.com")
                .nickname("newuser")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void join_success() {
        when(memberDomainService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(memberDomainService.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(memberDomainService.createMember(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testMember);

        var response = memberManagementService.join(joinRequest);

        assertThat(response).isNotNull();
        verify(memberDomainService).createMember(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 사용자명")
    void join_duplicateUsername() {
        when(memberDomainService.findByUsername("newuser")).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> memberManagementService.join(joinRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME);
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void join_duplicateEmail() {
        when(memberDomainService.findByUsername("newuser")).thenReturn(Optional.empty());
        when(memberDomainService.findByEmail("new@test.com")).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> memberManagementService.join(joinRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("회원 승인")
    void approveMember() {
        when(memberDomainService.approveMember(1L, 2L)).thenReturn(testMember);

        var response = memberManagementService.approveMember(1L, 2L);

        assertThat(response).isNotNull();
        verify(memberDomainService).approveMember(1L, 2L);
    }

    @Test
    @DisplayName("회원 거부")
    void rejectMember() {
        when(memberDomainService.rejectMember(1L)).thenReturn(testMember);

        var response = memberManagementService.rejectMember(1L);

        assertThat(response).isNotNull();
        verify(memberDomainService).rejectMember(1L);
    }

    @Test
    @DisplayName("회원 정지")
    void suspendMember() {
        when(memberDomainService.suspendMember(1L)).thenReturn(testMember);

        var response = memberManagementService.suspendMember(1L);

        assertThat(response).isNotNull();
        verify(memberDomainService).suspendMember(1L);
    }

    @Test
    @DisplayName("회원 잠금 해제")
    void unlockMember() {
        when(memberDomainService.unlockMember(1L)).thenReturn(testMember);

        var response = memberManagementService.unlockMember(1L);

        assertThat(response).isNotNull();
        verify(memberDomainService).unlockMember(1L);
    }

    @Test
    @DisplayName("프로필 업데이트")
    void updateProfile() {
        MemberUpdateRequest updateRequest = MemberUpdateRequest.builder()
                .nickname("newnickname")
                .email("newemail@test.com")
                .build();

        when(memberDomainService.updateMemberProfile(1L, "newnickname", "newemail@test.com")).thenReturn(testMember);

        var response = memberManagementService.updateProfile(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(memberCacheService).evictMemberCache(1L);
    }

    @Test
    @DisplayName("역할 변경")
    void changeRole() {
        when(memberDomainService.changeRole(1L, Role.MANAGER)).thenReturn(testMember);

        var response = memberManagementService.changeRole(1L, Role.MANAGER);

        assertThat(response).isNotNull();
        verify(memberDomainService).changeRole(1L, Role.MANAGER);
    }

    @Test
    @DisplayName("회원 역할 변경 - 성공")
    void changeMemberRole_success() {
        Member approver = Member.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(approver, "id", 2L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        var response = memberManagementService.changeMemberRole(1L, Role.MANAGER, 2L);

        assertThat(response).isNotNull();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 역할 변경 실패 - 동일한 역할")
    void changeMemberRole_sameRole() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> memberManagementService.changeMemberRole(1L, Role.USER, 2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("회원 역할 변경 실패 - 권한 없음")
    void changeMemberRole_noPermission() {
        Member approver = Member.builder()
                .username("user")
                .role(Role.USER)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(approver, "id", 2L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(approver));

        assertThatThrownBy(() -> memberManagementService.changeMemberRole(1L, Role.MANAGER, 2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("회원 삭제")
    void deleteMember() {
        when(memberDomainService.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberDomainService.save(any(Member.class))).thenReturn(testMember);

        memberManagementService.deleteMember(1L);

        verify(memberDomainService).save(any(Member.class));
        verify(memberCacheService).evictMemberCache(1L);
    }

    @Test
    @DisplayName("회원 삭제 실패 - 관리자")
    void deleteMember_admin() {
        testMember = Member.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testMember, "id", 1L);

        when(memberDomainService.findById(1L)).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> memberManagementService.deleteMember(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
    }
}
