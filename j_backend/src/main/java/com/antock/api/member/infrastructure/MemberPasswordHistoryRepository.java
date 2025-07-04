package com.antock.api.member.infrastructure;

import com.antock.api.member.domain.MemberPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberPasswordHistoryRepository extends JpaRepository<MemberPasswordHistory, Long> {

    @Query("SELECT mph FROM MemberPasswordHistory mph WHERE mph.member.id = :memberId ORDER BY mph.createdAt DESC")
    List<MemberPasswordHistory> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

    @Query("SELECT mph FROM MemberPasswordHistory mph WHERE mph.member.id = :memberId " +
            "AND mph.createdAt >= :fromDate ORDER BY mph.createdAt DESC")
    List<MemberPasswordHistory> findRecentPasswordHistory(@Param("memberId") Long memberId,
                                                          @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(mph) FROM MemberPasswordHistory mph WHERE mph.member.id = :memberId " +
            "AND mph.createdAt >= :fromDate")
    long countPasswordChangesAfter(@Param("memberId") Long memberId,
                                   @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT mph FROM MemberPasswordHistory mph WHERE mph.member.id = :memberId " +
            "ORDER BY mph.createdAt DESC LIMIT :limit")
    List<MemberPasswordHistory> findRecentPasswordHistoryWithLimit(@Param("memberId") Long memberId,
                                                                   @Param("limit") int limit);
}