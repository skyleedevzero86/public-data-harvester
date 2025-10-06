package com.antock.api.health.infrastructure;

import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.domain.SystemHealth;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemHealthRepository extends JpaRepository<SystemHealth, Long> {

        @Query("SELECT s FROM SystemHealth s WHERE s.overallStatus = :status ORDER BY s.checkedAt DESC")
        List<SystemHealth> findByOverallStatusOrderByCheckedAtDesc(@Param("status") HealthStatus status);

        @Query("SELECT s FROM SystemHealth s WHERE s.overallStatus = :status ORDER BY s.checkedAt DESC")
        Page<SystemHealth> findByOverallStatusOrderByCheckedAtDesc(@Param("status") HealthStatus status,
                        Pageable pageable);

        @Query("SELECT s FROM SystemHealth s WHERE s.checkedAt >= :fromDate ORDER BY s.checkedAt DESC")
        List<SystemHealth> findByCheckedAtAfterOrderByCheckedAtDesc(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT s FROM SystemHealth s WHERE s.checkedAt BETWEEN :startDate AND :endDate ORDER BY s.checkedAt DESC")
        List<SystemHealth> findByCheckedAtBetweenOrderByCheckedAtDesc(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT s FROM SystemHealth s WHERE s.expiresAt < :now ORDER BY s.checkedAt DESC")
        List<SystemHealth> findExpiredSystemHealth(@Param("now") LocalDateTime now);

        @Query("SELECT s FROM SystemHealth s WHERE s.expiresAt > :now AND s.checkedAt = (SELECT MAX(s2.checkedAt) FROM SystemHealth s2 WHERE s2.expiresAt > :now) ORDER BY s.id DESC LIMIT 1")
        Optional<SystemHealth> findLatestValidSystemHealth(@Param("now") LocalDateTime now);

        @Query("SELECT s FROM SystemHealth s WHERE s.checkedAt = (SELECT MAX(s2.checkedAt) FROM SystemHealth s2) ORDER BY s.id DESC LIMIT 1")
        Optional<SystemHealth> findLatest();

        @Query("SELECT s FROM SystemHealth s WHERE s.checkedAt >= :fromDate ORDER BY s.checkedAt DESC")
        Page<SystemHealth> findRecentSystemHealth(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

        @Query("SELECT s.overallStatus, COUNT(s) FROM SystemHealth s WHERE s.checkedAt >= :fromDate GROUP BY s.overallStatus")
        List<Object[]> getStatusCounts(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT AVG(CASE WHEN s.totalComponents > 0 THEN (s.healthyComponents * 100.0 / s.totalComponents) ELSE 0.0 END) FROM SystemHealth s WHERE s.checkedAt >= :fromDate")
        Double getAverageHealthPercentage(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT s FROM SystemHealth s WHERE s.totalComponents > 0 AND (s.healthyComponents * 100.0 / s.totalComponents) < :threshold ORDER BY s.checkedAt DESC")
        List<SystemHealth> findLowHealthPercentage(@Param("threshold") Double threshold);

        @Query("SELECT s FROM SystemHealth s WHERE s.checkedAt >= :fromDate ORDER BY s.checkedAt DESC")
        List<SystemHealth> findSystemHealthHistory(@Param("fromDate") LocalDateTime fromDate);

        @Query("DELETE FROM SystemHealth s WHERE s.checkedAt < :cutoffDate")
        int deleteOldSystemHealth(@Param("cutoffDate") LocalDateTime cutoffDate);

        @Query("SELECT s FROM SystemHealth s WHERE s.checkedAt >= :fromDate AND s.overallStatus = :status ORDER BY s.checkedAt DESC")
        List<SystemHealth> findByStatusAndDateRange(@Param("status") HealthStatus status,
                        @Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT s FROM SystemHealth s WHERE s.healthyComponents = s.totalComponents AND s.checkedAt >= :fromDate ORDER BY s.checkedAt DESC")
        List<SystemHealth> findFullyHealthySystems(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT s FROM SystemHealth s WHERE s.unhealthyComponents > 0 AND s.checkedAt >= :fromDate ORDER BY s.checkedAt DESC")
        List<SystemHealth> findUnhealthySystems(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT s FROM SystemHealth s ORDER BY s.checkedAt DESC LIMIT 1")
        Optional<SystemHealth> findTopByOrderByCheckedAtDesc();
}