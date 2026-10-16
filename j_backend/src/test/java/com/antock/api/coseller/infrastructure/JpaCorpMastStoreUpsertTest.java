package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("법인 마스터 upsert")
class JpaCorpMastStoreUpsertTest {

    @Mock
    private CorpMastRepository corpMastRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private JpaCorpMastStore store;

    @Test
    @DisplayName("같은 사업자번호는 추가하지 않고 내용을 갱신한다")
    void upsertUpdatesOnBizNoConflict() throws Exception {
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[] {1});
        CorpMast older = row("123-45-67890", "이전상호");
        CorpMast newer = row("123-45-67890", "변경상호");

        int written = store.upsertAll(List.of(older, newer));

        assertThat(written).isEqualTo(1);
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BatchPreparedStatementSetter> setter = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(sql.capture(), setter.capture());

        String updateClause = sql.getValue().substring(sql.getValue().indexOf("ON CONFLICT"));
        assertThat(sql.getValue()).contains("ON CONFLICT (biz_no) DO UPDATE");
        assertThat(updateClause).contains("rep_nm = COALESCE(NULLIF(EXCLUDED.rep_nm, ''), corp_mast.rep_nm)");
        assertThat(updateClause).contains("road_nm_addr = COALESCE(NULLIF(EXCLUDED.road_nm_addr, ''), corp_mast.road_nm_addr)");
        assertThat(updateClause).contains("modify_date = now()");
        assertThat(updateClause).doesNotContain("create_date");
        assertThat(setter.getValue().getBatchSize()).isEqualTo(1);

        PreparedStatement ps = org.mockito.Mockito.mock(PreparedStatement.class);
        setter.getValue().setValues(ps, 0);
        verify(ps).setString(2, "변경상호");
        verify(ps).setString(3, "123-45-67890");
    }

    private CorpMast row(String bizNo, String bizNm) {
        return CorpMast.builder()
                .sellerId("seller001")
                .bizNm(bizNm)
                .bizNo(bizNo)
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();
    }
}
