package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.corpRegNo IS NOT NULL AND c.corpRegNo != ''")
        long countValidCorpRegNo();

        @Query("SELECT COUNT(c) FROM CorpMast c WHERE c.regionCd IS NOT NULL AND c.regionCd != ''")
        long countValidRegionCd();

        @Query("SELECT c.siNm as city, c.sggNm as district, COUNT(c) as totalCount " +
                        "FROM CorpMast c " +
                        "WHERE c.siNm IS NOT NULL AND c.sggNm IS NOT NULL " +
                        "GROUP BY c.siNm, c.sggNm " +
                        "ORDER BY COUNT(c) DESC")
        List<Object[]> getRegionStats();

        @Query("SELECT c.siNm as city, c.sggNm as district, COUNT(c) as totalCount " +
                        "FROM CorpMast c " +
                        "WHERE c.siNm IS NOT NULL AND c.sggNm IS NOT NULL " +
                        "AND (:city IS NULL OR c.siNm = :city) " +
                        "AND (:district IS NULL OR c.sggNm = :district) " +
                        "GROUP BY c.siNm, c.sggNm " +
                        "ORDER BY COUNT(c) DESC")
        Page<Object[]> getRegionStatsWithPaging(
                        Pageable pageable,
                        @Param("city") String city,
                        @Param("district") String district);
}