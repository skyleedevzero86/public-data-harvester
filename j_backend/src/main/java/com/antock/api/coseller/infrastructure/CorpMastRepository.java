package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorpMastRepository extends JpaRepository<CorpMast, Long>, CorpMastRepositoryCustom {

        boolean existsByBizNo(String bizNo);

        Optional<CorpMast> findByBizNo(@Param("bizNo") String bizNo);

        @Query("SELECT c FROM CorpMast c WHERE c.siNm = :siNm AND c.sggNm = :sggNm")
        List<CorpMast> findBySiNmAndSggNm(@Param("siNm") String siNm, @Param("sggNm") String sggNm);

        @Query("SELECT c FROM CorpMast c WHERE c.siNm = :siNm AND c.sggNm = :sggNm")
        Page<CorpMast> findBySiNmAndSggNm(@Param("siNm") String siNm, @Param("sggNm") String sggNm, Pageable pageable);

        @Query("SELECT c FROM CorpMast c WHERE c.siNm = :siNm")
        List<CorpMast> findBySiNm(@Param("siNm") String siNm);

        @Query("SELECT c FROM CorpMast c WHERE c.siNm = :siNm")
        Page<CorpMast> findBySiNm(@Param("siNm") String siNm, Pageable pageable);

        @Query("SELECT DISTINCT c.siNm, c.sggNm FROM CorpMast c WHERE c.siNm LIKE %:city% OR c.sggNm LIKE %:district%")
        List<Object[]> findDistinctCityDistrict(@Param("city") String city, @Param("district") String district);

        @Query("SELECT DISTINCT c.siNm, c.sggNm, COUNT(c) FROM CorpMast c WHERE c.siNm LIKE %:city% GROUP BY c.siNm, c.sggNm ORDER BY COUNT(c) DESC")
        List<Object[]> findCityDistrictStats(@Param("city") String city);

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

        @Modifying
        @Query(value = "ALTER TABLE corp_mast " +
                        "ADD COLUMN IF NOT EXISTS rep_nm VARCHAR(100), " +
                        "ADD COLUMN IF NOT EXISTS estb_dt VARCHAR(20), " +
                        "ADD COLUMN IF NOT EXISTS road_nm_addr VARCHAR(200), " +
                        "ADD COLUMN IF NOT EXISTS jibun_addr VARCHAR(200), " +
                        "ADD COLUMN IF NOT EXISTS corp_status VARCHAR(50)", nativeQuery = true)
        void addMissingColumns();

        @Modifying
        @Query(value = "UPDATE corp_mast SET " +
                        "rep_nm = CASE " +
                        "    WHEN id % 5 = 0 THEN '김철수' " +
                        "    WHEN id % 5 = 1 THEN '이영희' " +
                        "    WHEN id % 5 = 2 THEN '박민수' " +
                        "    WHEN id % 5 = 3 THEN '정수진' " +
                        "    ELSE '최동현' " +
                        "END, " +
                        "estb_dt = CASE " +
                        "    WHEN id % 3 = 0 THEN '20200101' " +
                        "    WHEN id % 3 = 1 THEN '20210315' " +
                        "    ELSE '20220520' " +
                        "END, " +
                        "road_nm_addr = CASE " +
                        "    WHEN id % 4 = 0 THEN '울산광역시 중구 성남동 123-45' " +
                        "    WHEN id % 4 = 1 THEN '울산광역시 중구 학성동 678-90' " +
                        "    WHEN id % 4 = 2 THEN '울산광역시 중구 반구동 111-22' " +
                        "    ELSE '울산광역시 중구 다운동 333-44' " +
                        "END, " +
                        "jibun_addr = CASE " +
                        "    WHEN id % 4 = 0 THEN '울산광역시 중구 성남동 123-45' " +
                        "    WHEN id % 4 = 1 THEN '울산광역시 중구 학성동 678-90' " +
                        "    WHEN id % 4 = 2 THEN '울산광역시 중구 반구동 111-22' " +
                        "    ELSE '울산광역시 중구 다운동 333-44' " +
                        "END, " +
                        "corp_status = CASE " +
                        "    WHEN id % 7 = 0 THEN '계속(수익)' " +
                        "    WHEN id % 7 = 1 THEN '휴업' " +
                        "    WHEN id % 7 = 2 THEN '폐업' " +
                        "    WHEN id % 7 = 3 THEN '계속(수익)' " +
                        "    WHEN id % 7 = 4 THEN '계속(수익)' " +
                        "    WHEN id % 7 = 5 THEN '휴업' " +
                        "    ELSE '계속(수익)' " +
                        "END " +
                        "WHERE si_nm = '울산광역시' AND sgg_nm = '중구'", nativeQuery = true)
        void addSampleData();
}