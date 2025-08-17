package com.antock.api.member.infrastructure;

import com.antock.api.member.domain.Member;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    Optional<Member> findByUsername(String username);

    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    Optional<Member> findByEmail(String email);

    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    Optional<Member> findByApiKey(String apiKey);

    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    boolean existsByUsername(String username);

    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    boolean existsByEmail(String email);

    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    Page<Member> findByStatus(MemberStatus status, Pageable pageable);

    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    Page<Member> findByRole(Role role, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.status = :status AND m.createDate >= :fromDate")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    List<Member> findPendingMembersAfter(@Param("status") MemberStatus status,
                                         @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    long countByStatus(@Param("status") MemberStatus status);

    @Query("SELECT m FROM Member m WHERE m.accountLockedAt IS NOT NULL AND m.accountLockedAt < :unlockTime")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "100")
    })
    List<Member> findLockedMembersBeforeUnlockTime(@Param("unlockTime") LocalDateTime unlockTime);

    @Query("SELECT m FROM Member m WHERE m.passwordChangedAt < :beforeDate AND m.status = 'APPROVED'")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "100")
    })
    List<Member> findMembersWithPasswordChangedBefore(@Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.passwordChangedAt < :beforeDate AND m.status = 'APPROVED'")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    long countMembersWithPasswordChangedBefore(@Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT COUNT(mph) FROM MemberPasswordHistory mph WHERE mph.createdAt >= :fromDate")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    long countPasswordChangesAfter(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT m FROM Member m WHERE m.passwordChangedAt IS NULL AND m.status = 'APPROVED'")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "100")
    })
    List<Member> findMembersWithNullPasswordChangedAt();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.passwordChangedAt IS NULL AND m.status = 'APPROVED'")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    long countMembersWithNullPasswordChangedAt();

    @Query("SELECT m FROM Member m WHERE m.lastPasswordChangeDate = :date")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "100")
    })
    List<Member> findMembersByLastPasswordChangeDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.lastPasswordChangeDate = :date AND m.passwordChangeCount >= :count")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
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
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    Integer getCurrentLoginFailCount(@Param("memberId") Long memberId);

    @Query("SELECT m FROM Member m WHERE (:status IS NULL OR m.status = :status) AND (:role IS NULL OR m.role = :role)")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    Page<Member> findByStatusAndRole(@Param("status") MemberStatus status,
                                     @Param("role") Role role,
                                     Pageable pageable);

    @Query("SELECT m.id FROM Member m WHERE m.id IN :ids")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "1000")
    })
    List<Long> findIdsByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT NEW com.antock.api.member.application.dto.response.MemberStatsDto(m.status, COUNT(m)) FROM Member m GROUP BY m.status")
    @QueryHints({
            @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_CACHEABLE, value = "true")
    })
    List<com.antock.api.member.application.dto.response.MemberStatsDto> getMemberStats();
}
