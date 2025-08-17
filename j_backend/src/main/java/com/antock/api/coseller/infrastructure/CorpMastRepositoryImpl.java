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
    public List<RegionStatDto> getRegionStats() {
        String jpql = "SELECT c.siNm, c.sggNm, COUNT(c) as totalCount, " +
                "SUM(CASE WHEN c.corpRegNo IS NOT NULL AND c.corpRegNo <> '' AND c.corpRegNo NOT LIKE '0%' AND c.corpRegNo NOT LIKE '%N/A%' THEN 1 ELSE 0 END) as validCorpRegNoCount, " +
                "SUM(CASE WHEN c.regionCd IS NOT NULL AND c.regionCd <> '' AND c.regionCd NOT LIKE '0%' THEN 1 ELSE 0 END) as validRegionCdCount " +
                "FROM CorpMast c " +
                "GROUP BY c.siNm, c.sggNm " +
                "ORDER BY COUNT(c) DESC";

        List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();

        return results.stream()
                .map(row -> {
                    String city = (String) row[0];
                    String district = (String) row[1];
                    Long totalCount = (Long) row[2];
                    Long validCorpRegNoCount = (Long) row[3];
                    Long validRegionCdCount = (Long) row[4];

                    double completionRate = totalCount > 0 ?
                            (double) (validCorpRegNoCount + validRegionCdCount) / (totalCount * 2) * 100 : 0.0;

                    return RegionStatDto.builder()
                            .city(city)
                            .district(district)
                            .totalCount(totalCount)
                            .validCorpRegNoCount(validCorpRegNoCount)
                            .validRegionCdCount(validRegionCdCount)
                            .completionRate(completionRate)
                            .build();
                })
                .collect(Collectors.toList());
    }
}