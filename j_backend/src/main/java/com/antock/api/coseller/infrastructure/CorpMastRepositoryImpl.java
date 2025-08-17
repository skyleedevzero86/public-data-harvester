package com.antock.api.coseller.infrastructure;

import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.coseller.domain.CorpMast;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CorpMastRepositoryImpl implements CorpMastRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Object[]> getRegionStats() {
        String jpql = "SELECT c.siNm, c.sggNm, COUNT(c) as totalCount, " +
                "SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END) as validCorpRegNoCount, " +
                "SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END) as validRegionCdCount " +
                "FROM CorpMast c " +
                "GROUP BY c.siNm, c.sggNm " +
                "ORDER BY COUNT(c) DESC";

        return em.createQuery(jpql, Object[].class).getResultList();
    }
}