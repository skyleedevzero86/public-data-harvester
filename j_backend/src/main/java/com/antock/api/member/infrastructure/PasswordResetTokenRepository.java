package com.antock.api.member.infrastructure;

import com.antock.api.member.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.used = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.member.id = :memberId AND t.used = false AND t.expiresAt > :now ORDER BY t.createDate DESC")
    Optional<PasswordResetToken> findLatestValidTokenByMemberId(@Param("memberId") Long memberId,
            @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.member.id = :memberId AND t.used = false")
    void invalidateAllTokensByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.member.id = :memberId AND t.used = false")
    long countByMemberIdAndUsedFalse(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.member.id = :memberId AND t.createDate >= :since")
    long countRecentTokensByMemberId(@Param("memberId") Long memberId, @Param("since") LocalDateTime since);
}