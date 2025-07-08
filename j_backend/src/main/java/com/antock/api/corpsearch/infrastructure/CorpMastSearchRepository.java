package com.antock.api.corpsearch.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CorpMastSearchRepository extends JpaRepository<CorpMast, Long> {

    @Query("SELECT c FROM CorpMast c " +
            "WHERE (:bizNm IS NULL OR :bizNm = '' OR LOWER(c.bizNm) LIKE LOWER(CONCAT('%', :bizNm, '%'))) " +
            "AND (:bizNo IS NULL OR :bizNo = '' OR REPLACE(c.bizNo, '-', '') = REPLACE(:bizNo, '-', '')) " +
            "AND (:sellerId IS NULL OR :sellerId = '' OR LOWER(c.sellerId) LIKE LOWER(CONCAT('%', :sellerId, '%'))) " +
            "AND (:corpRegNo IS NULL OR :corpRegNo = '' OR c.corpRegNo = :corpRegNo) " +
            "AND (:city IS NULL OR :city = '' OR c.siNm = :city) " +
            "AND (:district IS NULL OR :district = '' OR c.sggNm = :district)")
    Page<CorpMast> findBySearchConditions(
            @Param("bizNm") String bizNm,
            @Param("bizNo") String bizNo,
            @Param("sellerId") String sellerId,
            @Param("corpRegNo") String corpRegNo,
            @Param("city") String city,
            @Param("district") String district,
            Pageable pageable);

    @Query("SELECT c FROM CorpMast c " +
            "WHERE (:bizNm IS NULL OR :bizNm = '' OR LOWER(c.bizNm) LIKE LOWER(CONCAT('%', :bizNm, '%'))) " +
            "AND (:bizNo IS NULL OR :bizNo = '' OR REPLACE(c.bizNo, '-', '') = REPLACE(:bizNo, '-', '')) " +
            "AND (:sellerId IS NULL OR :sellerId = '' OR LOWER(c.sellerId) LIKE LOWER(CONCAT('%', :sellerId, '%'))) " +
            "AND (:corpRegNo IS NULL OR :corpRegNo = '' OR c.corpRegNo = :corpRegNo) " +
            "AND (:city IS NULL OR :city = '' OR c.siNm = :city) " +
            "AND (:district IS NULL OR :district = '' OR c.sggNm = :district) " +
            "ORDER BY c.id DESC")
    List<CorpMast> findBySearchConditionsForExcel(
            @Param("bizNm") String bizNm,
            @Param("bizNo") String bizNo,
            @Param("sellerId") String sellerId,
            @Param("corpRegNo") String corpRegNo,
            @Param("city") String city,
            @Param("district") String district);

    @Query("SELECT COUNT(c) FROM CorpMast c " +
            "WHERE (:bizNm IS NULL OR :bizNm = '' OR LOWER(c.bizNm) LIKE LOWER(CONCAT('%', :bizNm, '%'))) " +
            "AND (:bizNo IS NULL OR :bizNo = '' OR REPLACE(c.bizNo, '-', '') = REPLACE(:bizNo, '-', '')) " +
            "AND (:sellerId IS NULL OR :sellerId = '' OR LOWER(c.sellerId) LIKE LOWER(CONCAT('%', :sellerId, '%'))) " +
            "AND (:corpRegNo IS NULL OR :corpRegNo = '' OR c.corpRegNo = :corpRegNo) " +
            "AND (:city IS NULL OR :city = '' OR c.siNm = :city) " +
            "AND (:district IS NULL OR :district = '' OR c.sggNm = :district)")
    long countBySearchConditions(
            @Param("bizNm") String bizNm,
            @Param("bizNo") String bizNo,
            @Param("sellerId") String sellerId,
            @Param("corpRegNo") String corpRegNo,
            @Param("city") String city,
            @Param("district") String district);

    @Query("SELECT c FROM CorpMast c WHERE REPLACE(c.bizNo, '-', '') = REPLACE(:bizNo, '-', '')")
    Optional<CorpMast> findByBizNo(@Param("bizNo") String bizNo);

    Optional<CorpMast> findByCorpRegNo(String corpRegNo);

    @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.siNm = :city AND (:district IS NULL OR c.sggNm = :district)")
    long countByLocation(@Param("city") String city, @Param("district") String district);

    Page<CorpMast> findBySellerIdContainingIgnoreCase(String sellerId, Pageable pageable);

    @Query("SELECT c FROM CorpMast c WHERE LOWER(c.bizNm) LIKE LOWER(CONCAT('%', :bizNm, '%'))")
    Page<CorpMast> findByBizNmContainingIgnoreCase(@Param("bizNm") String bizNm, Pageable pageable);

    @Query("SELECT DISTINCT c.sggNm FROM CorpMast c WHERE c.siNm = :city ORDER BY c.sggNm")
    List<String> findDistinctDistrictsByCity(@Param("city") String city);

    @Query("SELECT DISTINCT c.siNm FROM CorpMast c ORDER BY c.siNm")
    List<String> findDistinctCities();
}