package com.antock.api.member.application.service;

import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberDomainService {

    private final MemberRepository memberRepository;

    public Member createMember(String username, String password, String nickname, String email) {
        validateDuplicateUsername(username);
        validateDuplicateEmail(email);

        Member member = Member.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .email(email)
                .apiKey(generateApiKey())
                .status(MemberStatus.PENDING)
                .role(Role.USER)
                .build();

        return memberRepository.save(member);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public Page<Member> findMembersByStatus(MemberStatus status, Pageable pageable) {
        return memberRepository.findByStatus(status, pageable);
    }

    public Page<Member> findAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    public List<Member> findPendingMembers() {
        return memberRepository.findPendingMembersAfter(
                MemberStatus.PENDING,
                LocalDateTime.now().minusDays(30)
        );
    }

    public Member approveMember(Long memberId, Long approverId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() != MemberStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);
        }

        member.approve(approverId);
        return memberRepository.save(member);
    }

    public Member rejectMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.reject();
        return memberRepository.save(member);
    }

    public Member suspendMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.suspend();
        return memberRepository.save(member);
    }

    public Member updateMemberProfile(Long memberId, String nickname, String email) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.getEmail().equals(email)) {
            validateDuplicateEmail(email);
        }

        member.updateProfile(nickname, email);
        return memberRepository.save(member);
    }

    public Member changeRole(Long memberId, Role role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.changeRole(role);
        return memberRepository.save(member);
    }

    public void handleLoginSuccess(Member member) {
        member.resetLoginFailCount();
        member.updateLastLoginAt();
        memberRepository.save(member);
    }

    public void handleLoginFailure(Member member) {
        member.increaseLoginFailCount();
        memberRepository.save(member);
    }

    public long countMembersByStatus(MemberStatus status) {
        return memberRepository.countByStatus(status);
    }

    private void validateDuplicateUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}