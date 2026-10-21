package com.antock.api.corpmanual.application.service;

import com.antock.api.corpmanual.application.dto.request.CorpMastForm;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.infrastructure.CorpMastManualRepository;
import com.antock.api.coseller.domain.CorpMast;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CorpMastManualServiceListTest {

    @Mock
    private CorpMastManualRepository corpMastManualRepository;

    @Mock
    private Executor asyncExecutor;

    @InjectMocks
    private CorpMastManualService corpMastManualService;

    @Test
    @DisplayName("검색 조건이 없어도 관리자는 저장된 법인 목록을 조회한다")
    void findForList_withoutSearchCondition() {
        CorpMast saved = CorpMast.builder()
                .sellerId("seller")
                .bizNm("테스트법인")
                .bizNo("123-45-67890")
                .corpRegNo("110111-0000000")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강서구")
                .username("admin0001")
                .build();
        given(corpMastManualRepository.searchCorpMast(
                eq(true), eq("admin0001"), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(saved)));

        Page<CorpMastManualResponse> result = corpMastManualService.findForList(
                true, "admin0001", new CorpMastForm(), 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getBizNm()).isEqualTo("테스트법인");
        assertThat(result.getContent().get(0).getSiNm()).isEqualTo("서울특별시");
    }
}
