package com.antock.api.member.infrastructure;

import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
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

    @Query("SELECT m FROM Member m WHERE m.passwordChangedAt < :beforeDate AND m.status = 'APPROVED'")
    List<Member> findMembersWithPasswordChangedBefore(@Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.passwordChangedAt < :beforeDate AND m.status = 'APPROVED'")
    long countMembersWithPasswordChangedBefore(@Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT COUNT(mph) FROM MemberPasswordHistory mph WHERE mph.createdAt >= :fromDate")
    long countPasswordChangesAfter(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT m FROM Member m WHERE m.passwordChangedAt IS NULL AND m.status = 'APPROVED'")
    List<Member> findMembersWithNullPasswordChangedAt();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.passwordChangedAt IS NULL AND m.status = 'APPROVED'")
    long countMembersWithNullPasswordChangedAt();

    @Query("SELECT m FROM Member m WHERE m.lastPasswordChangeDate = :date")
    List<Member> findMembersByLastPasswordChangeDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.lastPasswordChangeDate = :date AND m.passwordChangeCount >= :count")
    long countMembersExceedingDailyLimit(@Param("date") LocalDate date, @Param("count") int count);

    @Modifying
    @Query(value = "UPDATE members SET login_fail_count = :failCount, status = :status, account_locked_at = :lockedAt, modify_date = NOW() WHERE id = :memberId", nativeQuery = true)
    int updateLoginFailBySql(@Param("memberId") Long memberId,
                             @Param("failCount") Integer failCount,
                             @Param("status") String status,
                             @Param("lockedAt") LocalDateTime lockedAt);

    @Modifying
    @Query(value = "UPDATE members SET login_fail_count = :failCount, modify_date = NOW() WHERE id = :memberId", nativeQuery = true)
    int updateLoginFailCountOnly(@Param("memberId") Long memberId, @Param("failCount") Integer failCount);

    @Query(value = "SELECT login_fail_count FROM members WHERE id = :memberId", nativeQuery = true)
    Integer getCurrentLoginFailCount(@Param("memberId") Long memberId);

    @Query("SELECT m FROM Member m WHERE (:status IS NULL OR m.status = :status) AND (:role IS NULL OR m.role = :role)")
    Page<Member> findByStatusAndRole(@Param("status") MemberStatus status,
                                     @Param("role") Role role,
                                     Pageable pageable);

}