package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CorpMastRepository extends JpaRepository<CorpMast, Long>, CorpMastRepositoryCustom {

    boolean existsByBizNo(String bizNo);
    Optional<CorpMast> findByBizNo(@Param("bizNo") String bizNo);
    List<CorpMast> findBySiNmAndSggNm(String siNm, String sggNm);
    List<CorpMast> findBySiNm(String siNm);

    @Query("SELECT c.bizNo FROM CorpMast c WHERE c.bizNo IN :bizNos")
    List<String> findBizNosByBizNoIn(@Param("bizNos") List<String> bizNos);

    long count();
    @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%'")
    long countValidCorpRegNo();
    @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%'")
    long countValidRegionCd();

    @Query("SELECT c.siNm, c.sggNm, COUNT(c) as totalCount, " +
            "SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END) as validCorpRegNoCount, " +
            "SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END) as validRegionCdCount " +
            "FROM CorpMast c " +
            "GROUP BY c.siNm, c.sggNm " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> getRegionStats();
}