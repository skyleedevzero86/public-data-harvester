package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CorpMastRepositoryCustom {
    @Query("SELECT c.siNm, c.sggNm, COUNT(c) as totalCount, " +
            "SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END) as validCorpRegNoCount, " +
            "SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END) as validRegionCdCount " +
            "FROM CorpMast c " +
            "GROUP BY c.siNm, c.sggNm " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> getRegionStats();
}