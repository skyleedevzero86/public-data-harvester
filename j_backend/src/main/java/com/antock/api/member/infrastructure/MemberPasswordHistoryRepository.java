package com.antock.api.member.infrastructure;

import com.antock.api.member.domain.MemberPasswordHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberPasswordHistoryRepository extends JpaRepository<MemberPasswordHistory, Long> {

        @Query("SELECT mph FROM MemberPasswordHistory mph " +
                        "WHERE mph.member.id = :memberId " +
                        "ORDER BY mph.createdAt DESC")
        List<MemberPasswordHistory> findRecentPasswordHistoryWithLimit(
                        @Param("memberId") Long memberId,
                        Pageable pageable);

        default List<MemberPasswordHistory> findRecentPasswordHistoryWithLimit(Long memberId, int limit) {
                Pageable pageable = PageRequest.of(0, limit);
                return findRecentPasswordHistoryWithLimit(memberId, pageable);
        }

        @Query("SELECT COUNT(mph) FROM MemberPasswordHistory mph " +
                        "WHERE mph.member.id = :memberId " +
                        "AND mph.createdAt >= :afterDateTime")
        long countPasswordChangesAfter(
                        @Param("memberId") Long memberId,
                        @Param("afterDateTime") LocalDateTime afterDateTime);

        @Query("SELECT mph FROM MemberPasswordHistory mph " +
                        "WHERE mph.member.id = :memberId " +
                        "ORDER BY mph.createdAt DESC")
        List<MemberPasswordHistory> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);

        @Query("SELECT COUNT(mph) FROM MemberPasswordHistory mph " +
                        "WHERE mph.member.id = :memberId " +
                        "AND DATE(mph.createdAt) = DATE(:today)")
        long countTodayPasswordChanges(
                        @Param("memberId") Long memberId,
                        @Param("today") LocalDateTime today);
}