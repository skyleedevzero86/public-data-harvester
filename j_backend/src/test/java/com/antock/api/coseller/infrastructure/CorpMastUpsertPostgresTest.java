package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Testcontainers(disabledWithoutDocker = true)
class CorpMastUpsertPostgresTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void blankApiFieldsDoNotEraseStoredValues() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUsername(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("""
                CREATE TABLE corp_mast (
                    seller_id varchar(100) NOT NULL,
                    biz_nm varchar(200) NOT NULL,
                    biz_no varchar(20) PRIMARY KEY,
                    corp_reg_no varchar(20) NOT NULL,
                    region_cd varchar(20) NOT NULL,
                    si_nm varchar(50) NOT NULL,
                    sgg_nm varchar(50) NOT NULL,
                    username varchar(100) NOT NULL,
                    rep_nm varchar(100),
                    estb_dt varchar(20),
                    road_nm_addr varchar(200),
                    jibun_addr varchar(200),
                    corp_status varchar(50),
                    create_date timestamp,
                    modify_date timestamp
                )
                """);
        jdbc.update("""
                INSERT INTO corp_mast (
                    seller_id, biz_nm, biz_no, corp_reg_no, region_cd, si_nm, sgg_nm, username,
                    rep_nm, road_nm_addr, create_date, modify_date
                ) VALUES ('seller', '이전상호', '1234567890', '1101110000000', '11680',
                          '서울특별시', '강남구', 'owner', '홍길동', '테헤란로 1', now(), now())
                """);

        JpaCorpMastStore store = new JpaCorpMastStore(mock(CorpMastRepository.class), jdbc);
        CorpMast fallback = CorpMast.builder()
                .sellerId("seller")
                .bizNm("변경상호")
                .bizNo("1234567890")
                .corpRegNo("")
                .regionCd("")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("owner")
                .repNm("")
                .roadNmAddr("")
                .build();

        int written = store.upsertAll(List.of(fallback));

        assertThat(written).isEqualTo(1);
        String repNm = jdbc.queryForObject("SELECT rep_nm FROM corp_mast WHERE biz_no = '1234567890'", String.class);
        String road = jdbc.queryForObject("SELECT road_nm_addr FROM corp_mast WHERE biz_no = '1234567890'", String.class);
        String bizNm = jdbc.queryForObject("SELECT biz_nm FROM corp_mast WHERE biz_no = '1234567890'", String.class);
        assertThat(repNm).isEqualTo("홍길동");
        assertThat(road).isEqualTo("테헤란로 1");
        assertThat(bizNm).isEqualTo("변경상호");
    }
}
