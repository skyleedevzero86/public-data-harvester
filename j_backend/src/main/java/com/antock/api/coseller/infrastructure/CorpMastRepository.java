package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
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

    long count();
    @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%'")
    long countValidCorpRegNo();
    @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%'")
    long countValidRegionCd();

}