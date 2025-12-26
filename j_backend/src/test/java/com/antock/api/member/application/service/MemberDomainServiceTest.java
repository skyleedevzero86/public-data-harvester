package com.antock.api.member.application.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberDomainService 테스트")
class MemberDomainServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberDomainService memberDomainService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .username("testuser")
                .password("encoded")
                .nickname("test")
                .email("test@test.com")
                .status(MemberStatus.APPROVED)
                .role(Role.USER)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testMember, "id", 1L);
    }

    @Test
    @DisplayName("회원 생성 성공")
    void createMember_success() {
        when(memberRepository.existsByUsername("testuser")).thenReturn(false);
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberDomainService.createMember("testuser", "password", "test", "test@test.com");

        assertThat(result).isNotNull();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 생성 - 중복 사용자명")
    void createMember_duplicateUsername() {
        when(memberRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> memberDomainService.createMember("testuser", "password", "test", "test@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USERNAME);
    }

    @Test
    @DisplayName("회원 생성 - 중복 이메일")
    void createMember_duplicateEmail() {
        when(memberRepository.existsByUsername("testuser")).thenReturn(false);
        when(memberRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> memberDomainService.createMember("testuser", "password", "test", "test@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("사용자명으로 조회")
    void findByUsername() {
        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(testMember));

        Optional<Member> result = memberDomainService.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("이메일로 조회")
    void findByEmail() {
        when(memberRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testMember));

        Optional<Member> result = memberDomainService.findByEmail("test@test.com");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("ID로 조회")
    void findById() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        Optional<Member> result = memberDomainService.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("상태별 회원 조회")
    void findMembersByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(Arrays.asList(testMember));
        when(memberRepository.findByStatus(MemberStatus.APPROVED, pageable)).thenReturn(page);

        Page<Member> result = memberDomainService.findMembersByStatus(MemberStatus.APPROVED, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("회원 승인")
    void approveMember() {
        Member pendingMember = Member.builder()
                .username("pendinguser")
                .password("encoded")
                .nickname("pending")
                .email("pending@test.com")
                .status(MemberStatus.PENDING)
                .role(Role.USER)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(pendingMember, "id", 2L);
        when(memberRepository.findById(2L)).thenReturn(Optional.of(pendingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(pendingMember);

        Member result = memberDomainService.approveMember(2L, 1L);

        assertThat(result).isNotNull();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 승인 - 회원 없음")
    void approveMember_notFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberDomainService.approveMember(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원 거부")
    void rejectMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberDomainService.rejectMember(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("회원 정지")
    void suspendMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberDomainService.suspendMember(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("프로필 업데이트")
    void updateMemberProfile() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberDomainService.updateMemberProfile(1L, "newnick", "new@test.com");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("역할 변경")
    void changeRole() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberDomainService.changeRole(1L, Role.ADMIN);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("회원 잠금 해제")
    void unlockMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member result = memberDomainService.unlockMember(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("회원 저장")
    void save() {
        when(memberRepository.save(testMember)).thenReturn(testMember);

        Member result = memberDomainService.save(testMember);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("회원 수 조회")
    void countAllMembers() {
        when(memberRepository.count()).thenReturn(10L);

        long count = memberDomainService.countAllMembers();

        assertThat(count).isEqualTo(10L);
    }
}

