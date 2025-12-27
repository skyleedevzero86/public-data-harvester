package com.antock.api.coseller.infrastructure;

import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.coseller.domain.CorpMast;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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

    @Deprecated(since = "2024-01-15", forRemoval = true)
    @Override
    public void addMissingColumns() {
        throw new UnsupportedOperationException(
            "런타임 DDL 실행은 지원되지 않습니다. " +
            "데이터베이스 스키마 변경은 Flyway 마이그레이션 스크립트를 통해 관리해야 합니다. " +
            "자세한 내용은 src/main/resources/db/migration/V2__Add_corp_mast_additional_columns.sql을 참조하세요."
        );
    }

    @Override
    public void addSampleData() {
        List<CorpMast> corpMasts = em.createQuery(
            "SELECT c FROM CorpMast c WHERE c.siNm = :siNm AND c.sggNm = :sggNm",
            CorpMast.class
        )
        .setParameter("siNm", "울산광역시")
        .setParameter("sggNm", "중구")
        .getResultList();

        for (CorpMast corpMast : corpMasts) {
            long id = corpMast.getId();
            
            String repNm = switch ((int)(id % 5)) {
                case 0 -> "김철수";
                case 1 -> "이영희";
                case 2 -> "박민수";
                case 3 -> "정수진";
                default -> "최동현";
            };
            
            String estbDt = switch ((int)(id % 3)) {
                case 0 -> "20200101";
                case 1 -> "20210315";
                default -> "20220520";
            };
            
            String roadNmAddr = switch ((int)(id % 4)) {
                case 0 -> "울산광역시 중구 성남동 123-45";
                case 1 -> "울산광역시 중구 학성동 678-90";
                case 2 -> "울산광역시 중구 반구동 111-22";
                default -> "울산광역시 중구 다운동 333-44";
            };
            
            String jibunAddr = roadNmAddr;
            
            String corpStatus = switch ((int)(id % 7)) {
                case 0, 3, 4, 6 -> "계속(수익)";
                case 1, 5 -> "휴업";
                case 2 -> "폐업";
                default -> "계속(수익)";
            };
            
            corpMast.setRepNm(repNm);
            corpMast.setEstbDt(estbDt);
            corpMast.setRoadNmAddr(roadNmAddr);
            corpMast.setJibunAddr(jibunAddr);
            corpMast.setCorpStatus(corpStatus);
            
            em.merge(corpMast);
        }
        
        em.flush();
    }
}