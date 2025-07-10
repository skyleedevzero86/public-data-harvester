package com.antock.api.coseller.infrastructure;

import com.antock.api.dashboard.application.dto.RegionStatDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CorpMastRepositoryImpl implements CorpMastRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<RegionStatDto> getRegionStats() {
        String jpql = """
            SELECT new com.antock.api.dashboard.application.dto.RegionStatDto(
                c.siNm, c.sggNm,
                COUNT(c),
                SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END),
                SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END),
                ((SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END) +
                  SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END)) / 
                  (2.0 * COUNT(c)) * 100.0)
            )
            FROM CorpMast c
            GROUP BY c.siNm, c.sggNm
            ORDER BY COUNT(c) DESC
        """;
        return em.createQuery(jpql, RegionStatDto.class).getResultList();
    }
}