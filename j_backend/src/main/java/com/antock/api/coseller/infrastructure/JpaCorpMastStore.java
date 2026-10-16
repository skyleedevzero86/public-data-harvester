package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaCorpMastStore implements CorpMastStore {

    private static final String UPSERT_SQL = """
            INSERT INTO corp_mast (
                seller_id, biz_nm, biz_no, corp_reg_no, region_cd, si_nm, sgg_nm, username,
                rep_nm, estb_dt, road_nm_addr, jibun_addr, corp_status, create_date, modify_date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now())
            ON CONFLICT (biz_no) DO UPDATE SET
                seller_id = EXCLUDED.seller_id,
                biz_nm = EXCLUDED.biz_nm,
                corp_reg_no = EXCLUDED.corp_reg_no,
                region_cd = EXCLUDED.region_cd,
                si_nm = EXCLUDED.si_nm,
                sgg_nm = EXCLUDED.sgg_nm,
                username = EXCLUDED.username,
                rep_nm = EXCLUDED.rep_nm,
                estb_dt = EXCLUDED.estb_dt,
                road_nm_addr = EXCLUDED.road_nm_addr,
                jibun_addr = EXCLUDED.jibun_addr,
                corp_status = EXCLUDED.corp_status,
                modify_date = now()
            """;

    private final CorpMastRepository corpMastRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(CorpMast corpMast) {
        corpMastRepository.save(corpMast);
    }

    @Override
    public void saveAll(List<CorpMast> entityList) {
        corpMastRepository.saveAll(entityList);
    }

    @Override
    public int upsertAll(List<CorpMast> rows) {
        List<CorpMast> uniqueRows = lastRowByBizNo(rows);
        if (uniqueRows.isEmpty()) {
            return 0;
        }
        int[] counts = jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CorpMast row = uniqueRows.get(i);
                ps.setString(1, text(row.getSellerId()));
                ps.setString(2, text(row.getBizNm()));
                ps.setString(3, text(row.getBizNo()));
                ps.setString(4, text(row.getCorpRegNo()));
                ps.setString(5, text(row.getRegionCd()));
                ps.setString(6, text(row.getSiNm()));
                ps.setString(7, text(row.getSggNm()));
                ps.setString(8, text(row.getUsername()));
                ps.setString(9, text(row.getRepNm()));
                ps.setString(10, text(row.getEstbDt()));
                ps.setString(11, text(row.getRoadNmAddr()));
                ps.setString(12, text(row.getJibunAddr()));
                ps.setString(13, text(row.getCorpStatus()));
            }

            @Override
            public int getBatchSize() {
                return uniqueRows.size();
            }
        });
        int written = 0;
        for (int count : counts) {
            if (count > 0 || count == Statement.SUCCESS_NO_INFO) {
                written++;
            }
        }
        return written;
    }

    @Override
    public boolean existsByBizNo(String bizNo) {
        return corpMastRepository.existsByBizNo(bizNo);
    }

    @Override
    public Optional<CorpMast> findByBizNo(String bizNo) {
        return corpMastRepository.findByBizNo(bizNo);
    }

    @Override
    public List<String> findExistingBizNos(List<String> bizNos) {
        return corpMastRepository.findBizNosByBizNoIn(bizNos);
    }

    @Override
    public List<CorpMast> findAll() {
        return corpMastRepository.findAll();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(CorpMast corpMast) {
        corpMastRepository.delete(corpMast);
    }

    private List<CorpMast> lastRowByBizNo(List<CorpMast> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        Map<String, CorpMast> byBizNo = new LinkedHashMap<>();
        for (CorpMast row : rows) {
            if (row == null) {
                continue;
            }
            byBizNo.put(text(row.getBizNo()), row);
        }
        return new ArrayList<>(byBizNo.values());
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}