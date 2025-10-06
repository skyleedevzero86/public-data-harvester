package com.antock.api.health.infrastructure;

import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
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
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByComponentOrderByCheckedAtDesc(@Param("component") String component);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findByComponentOrderByCheckedAtDesc(@Param("component") String component, Pageable pageable);

        @Query("SELECT h FROM HealthCheck h WHERE h.status = :status ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByStatusOrderByCheckedAtDesc(@Param("status") HealthStatus status);

        @Query("SELECT h FROM HealthCheck h WHERE h.status = :status ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findByStatusOrderByCheckedAtDesc(@Param("status") HealthStatus status, Pageable pageable);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.status = :status ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByComponentAndStatusOrderByCheckedAtDesc(@Param("component") String component,
                        @Param("status") HealthStatus status);

        @Query("SELECT h FROM HealthCheck h WHERE h.checkedAt >= :fromDate ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByCheckedAtAfterOrderByCheckedAtDesc(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.checkedAt BETWEEN :startDate AND :endDate ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByCheckedAtBetweenOrderByCheckedAtDesc(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.expiresAt < :now ORDER BY h.checkedAt DESC")
        List<HealthCheck> findExpiredChecks(@Param("now") LocalDateTime now);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.expiresAt > :now ORDER BY h.checkedAt DESC")
        Optional<HealthCheck> findLatestValidCheck(@Param("component") String component,
                        @Param("now") LocalDateTime now);

        @Query("SELECT h FROM HealthCheck h WHERE h.expiresAt > :now ORDER BY h.checkedAt DESC")
        List<HealthCheck> findAllValidChecks(@Param("now") LocalDateTime now);

        @Query("SELECT h.component, COUNT(h) FROM HealthCheck h WHERE h.checkedAt >= :fromDate GROUP BY h.component")
        List<Object[]> getComponentCheckCounts(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h.status, COUNT(h) FROM HealthCheck h WHERE h.checkedAt >= :fromDate GROUP BY h.status")
        List<Object[]> getStatusCounts(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.checkedAt = (SELECT MAX(h2.checkedAt) FROM HealthCheck h2 WHERE h2.component = :component) ORDER BY h.id DESC")
        Optional<HealthCheck> findLatestByComponent(@Param("component") String component);

        @Query("SELECT h FROM HealthCheck h WHERE h.checkType = :checkType ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByCheckTypeOrderByCheckedAtDesc(@Param("checkType") String checkType);

        @Query("SELECT h FROM HealthCheck h WHERE h.checkType = :checkType ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findByCheckTypeOrderByCheckedAtDesc(@Param("checkType") String checkType, Pageable pageable);

        @Query("SELECT h FROM HealthCheck h WHERE h.responseTime > :threshold ORDER BY h.checkedAt DESC")
        List<HealthCheck> findSlowChecks(@Param("threshold") Long threshold);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.responseTime > :threshold ORDER BY h.checkedAt DESC")
        List<HealthCheck> findSlowChecksByComponent(@Param("component") String component,
                        @Param("threshold") Long threshold);

        @Query("SELECT AVG(h.responseTime) FROM HealthCheck h WHERE h.component = :component AND h.checkedAt >= :fromDate")
        Double getAverageResponseTime(@Param("component") String component, @Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.checkedAt >= :fromDate ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findRecentChecks(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

        @Query("DELETE FROM HealthCheck h WHERE h.checkedAt < :cutoffDate")
        int deleteOldChecks(@Param("cutoffDate") LocalDateTime cutoffDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.component IN :components ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByComponentInOrderByCheckedAtDesc(@Param("components") List<String> components);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.checkedAt BETWEEN :startDate AND :endDate ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByComponentAndCheckedAtBetweenOrderByCheckedAtDesc(@Param("component") String component,
                        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT h.component, AVG(h.responseTime) FROM HealthCheck h WHERE h.checkedAt >= :fromDate AND h.responseTime IS NOT NULL GROUP BY h.component")
        List<Object[]> getComponentAverageResponseTimes(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h.component, h.status, COUNT(h) FROM HealthCheck h WHERE h.checkedAt >= :fromDate GROUP BY h.component, h.status")
        List<Object[]> getComponentStatusCounts(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.checkedAt >= :fromDate ORDER BY h.checkedAt DESC")
        List<HealthCheck> findRecentChecksByComponent(@Param("component") String component,
                        @Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.status = :status AND h.checkedAt >= :fromDate ORDER BY h.checkedAt DESC")
        List<HealthCheck> findByStatusAndCheckedAtAfterOrderByCheckedAtDesc(@Param("status") HealthStatus status,
                        @Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h FROM HealthCheck h WHERE h.responseTime > :threshold AND h.checkedAt >= :fromDate ORDER BY h.checkedAt DESC")
        List<HealthCheck> findSlowChecks(@Param("threshold") Long threshold, @Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h.component, MAX(h.responseTime) FROM HealthCheck h WHERE h.checkedAt >= :fromDate AND h.responseTime IS NOT NULL GROUP BY h.component")
        List<Object[]> getComponentMaxResponseTimes(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h.component, MIN(h.responseTime) FROM HealthCheck h WHERE h.checkedAt >= :fromDate AND h.responseTime IS NOT NULL GROUP BY h.component")
        List<Object[]> getComponentMinResponseTimes(@Param("fromDate") LocalDateTime fromDate);

        @Query("SELECT h FROM HealthCheck h ORDER BY h.checkedAt DESC")
        List<HealthCheck> findAllOrderByCheckedAtDesc();

        @Query("SELECT DISTINCT h.component FROM HealthCheck h ORDER BY h.component")
        List<String> findDistinctComponents();

        @Query("SELECT h FROM HealthCheck h WHERE h.checkedAt BETWEEN :startDate AND :endDate ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate, Pageable pageable);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.checkedAt BETWEEN :startDate AND :endDate ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findByComponentAndDateRange(@Param("component") String component,
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate, Pageable pageable);

        @Query("SELECT h FROM HealthCheck h WHERE h.status = :status AND h.checkedAt BETWEEN :startDate AND :endDate ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findByStatusAndDateRange(@Param("status") HealthStatus status,
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate, Pageable pageable);

        @Query("SELECT h FROM HealthCheck h WHERE h.component = :component AND h.status = :status AND h.checkedAt BETWEEN :startDate AND :endDate ORDER BY h.checkedAt DESC")
        Page<HealthCheck> findByComponentAndStatusAndDateRange(@Param("component") String component,
                                                             @Param("status") HealthStatus status,
                                                             @Param("startDate") LocalDateTime startDate, 
                                                             @Param("endDate") LocalDateTime endDate, Pageable pageable);
}