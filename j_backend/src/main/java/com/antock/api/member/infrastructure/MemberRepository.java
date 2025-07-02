package com.antock.api.member.infrastructure;

import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByApiKey(String apiKey);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Page<Member> findByStatus(MemberStatus status, Pageable pageable);
    Page<Member> findByRole(Role role, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.createDate >= :fromDate")
    List<Member> findPendingMembersAfter(@Param("status") MemberStatus status,
                                         @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status")
    long countByStatus(@Param("status") MemberStatus status);

    @Query("SELECT m FROM Member m WHERE m.accountLockedAt IS NOT NULL AND m.accountLockedAt < :unlockTime")
    List<Member> findLockedMembersBeforeUnlockTime(@Param("unlockTime") LocalDateTime unlockTime);
}
