package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.response.MemberResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberQueryService 테스트")
class MemberQueryServiceTest {

    @Mock
    private MemberDomainService memberDomainService;

    @InjectMocks
    private MemberQueryService memberQueryService;

    private Member testMember;
    private Pageable pageable;

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

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberInfo_success() {
        when(memberDomainService.findById(1L)).thenReturn(Optional.of(testMember));

        MemberResponse response = memberQueryService.getMemberInfo(1L);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 회원 없음")
    void getMemberInfo_notFound() {
        when(memberDomainService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberQueryService.getMemberInfo(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 목록 조회")
    void getMembers() {
        Page<Member> memberPage = new PageImpl<>(Arrays.asList(testMember));
        when(memberDomainService.findAllMembers(pageable)).thenReturn(memberPage);

        Page<MemberResponse> response = memberQueryService.getMembers(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("승인 대기 회원 목록 조회")
    void getPendingMembers() {
        Page<Member> memberPage = new PageImpl<>(Arrays.asList(testMember));
        when(memberDomainService.findMembersByStatus(MemberStatus.PENDING, pageable)).thenReturn(memberPage);

        Page<MemberResponse> response = memberQueryService.getPendingMembers(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("전체 회원 목록 조회")
    void getAllMembers() {
        List<Member> members = Arrays.asList(testMember);
        when(memberDomainService.findAllMembers()).thenReturn(members);

        List<MemberResponse> response = memberQueryService.getAllMembers();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }

    @Test
    @DisplayName("상태 및 역할로 회원 조회")
    void getMembersByStatusAndRole() {
        Page<Member> memberPage = new PageImpl<>(Arrays.asList(testMember));
        when(memberDomainService.findMembersByStatusAndRole("ACTIVE", "USER", pageable)).thenReturn(memberPage);

        Page<MemberResponse> response = memberQueryService.getMembersByStatusAndRole("ACTIVE", "USER", pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("사용자명으로 회원 ID 조회")
    void getMemberIdByUsername() {
        when(memberDomainService.findByUsername("testuser")).thenReturn(Optional.of(testMember));

        Long memberId = memberQueryService.getMemberIdByUsername("testuser");

        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자명으로 회원 ID 조회 - 회원 없음")
    void getMemberIdByUsername_notFound() {
        when(memberDomainService.findByUsername("unknown")).thenReturn(Optional.empty());

        Long memberId = memberQueryService.getMemberIdByUsername("unknown");

        assertThat(memberId).isNull();
    }

    @Test
    @DisplayName("상태별 회원 수 조회")
    void countMembersByStatus() {
        when(memberDomainService.countMembersByStatus(MemberStatus.APPROVED)).thenReturn(5L);

        long count = memberQueryService.countMembersByStatus(MemberStatus.APPROVED);

        assertThat(count).isEqualTo(5L);
    }
}
