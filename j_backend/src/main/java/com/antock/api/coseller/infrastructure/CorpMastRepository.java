package com.antock.api.coseller.infrastructure;

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
public interface CorpMastRepository extends JpaRepository<CorpMast, Long>, CorpMastRepositoryCustom {

        boolean existsByBizNo(String bizNo);

        Optional<CorpMast> findByBizNo(@Param("bizNo") String bizNo);

        List<CorpMast> findBySiNmAndSggNm(String siNm, String sggNm);

        List<CorpMast> findBySiNm(String siNm);

        @Query("SELECT c.bizNo FROM CorpMast c WHERE c.bizNo IN :bizNos")
        List<String> findBizNosByBizNoIn(@Param("bizNos") List<String> bizNos);

        long count();

        @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.corpRegNo IS NOT NULL AND c.corpRegNo != ''")
        long countValidCorpRegNo();

        @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.regionCd IS NOT NULL AND c.regionCd != ''")
        long countValidRegionCd();

        @Query("SELECT c.siNm as city, c.sggNm as district, COUNT(c) as totalCount, " +
                        "SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END) as validCorpRegNoCount, "
                        +
                        "SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END) as validRegionCdCount "
                        +
                        "FROM CorpMast c " +
                        "WHERE c.siNm IS NOT NULL AND c.sggNm IS NOT NULL " +
                        "GROUP BY c.siNm, c.sggNm " +
                        "ORDER BY COUNT(c) DESC")
        List<Object[]> getRegionStats();

        @Query("SELECT c.siNm as city, c.sggNm as district, COUNT(c) as totalCount, " +
                        "SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END) as validCorpRegNoCount, "
                        +
                        "SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END) as validRegionCdCount "
                        +
                        "FROM CorpMast c " +
                        "WHERE c.siNm IS NOT NULL AND c.sggNm IS NOT NULL " +
                        "AND (:city IS NULL OR :city = '' OR c.siNm = :city) " +
                        "AND (:district IS NULL OR :district = '' OR c.sggNm = :district) " +
                        "GROUP BY c.siNm, c.sggNm " +
                        "ORDER BY COUNT(c) DESC")
        Page<Object[]> getRegionStatsWithPaging(
                        Pageable pageable,
                        @Param("city") String city,
                        @Param("district") String district);

        @Query("SELECT DISTINCT c.siNm FROM CorpMast c WHERE c.siNm IS NOT NULL AND c.siNm != '' ORDER BY c.siNm")
        List<String> findDistinctCities();

        @Query("SELECT DISTINCT c.sggNm FROM CorpMast c WHERE c.siNm = :city AND c.sggNm IS NOT NULL AND c.sggNm != '' ORDER BY c.sggNm")
        List<String> findDistinctDistrictsByCity(@Param("city") String city);
}