package com.antock.api.corpmanual.application.service;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.infrastructure.CorpMastManualRepository;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorpMastManualService 테스트")
class CorpMastManualServiceTest {

    @Mock
    private CorpMastManualRepository corpMastManualRepository;

    @Mock
    private Executor asyncExecutor;

    @InjectMocks
    private CorpMastManualService corpMastManualService;

    private CorpMast testCorpMast;
    private CorpMastManualRequest request;

    @BeforeEach
    void setUp() {
        testCorpMast = CorpMast.builder()
                .sellerId("seller1")
                .bizNm("테스트 법인")
                .bizNo("123-45-67890")
                .corpRegNo("1234567890123")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .description("테스트 설명")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testCorpMast, "id", 1L);

        request = new CorpMastManualRequest();
        request.setBizNm("테스트");
    }

    @Test
    @DisplayName("법인 정보 검색")
    void search() {
        Page<CorpMast> page = new PageImpl<>(Arrays.asList(testCorpMast));
        when(corpMastManualRepository.findBySearchConditions(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        List<com.antock.api.corpmanual.application.dto.response.CorpMastSearchResponse> result =
                corpMastManualService.search(request);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("법인 정보 페이징 검색")
    void search_withPaging() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CorpMast> page = new PageImpl<>(Arrays.asList(testCorpMast));
        when(corpMastManualRepository.findBySearchConditions(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<CorpMastManualResponse> result = corpMastManualService.search(request, pageable);

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("ID로 법인 정보 조회")
    void getById() {
        when(corpMastManualRepository.findById(1L)).thenReturn(Optional.of(testCorpMast));

        CorpMastManualResponse response = corpMastManualService.getById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getBizNm()).isEqualTo("테스트 법인");
    }

    @Test
    @DisplayName("ID로 법인 정보 조회 - 없음")
    void getById_notFound() {
        when(corpMastManualRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> corpMastManualService.getById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CORP_NOT_FOUND);
    }

    @Test
    @DisplayName("사업자번호로 법인 정보 조회")
    void getByBizNo() {
        when(corpMastManualRepository.findByBizNo("1234567890")).thenReturn(Optional.of(testCorpMast));

        CorpMastManualResponse response = corpMastManualService.getByBizNo("123-45-67890");

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("법인등록번호로 법인 정보 조회")
    void getByCorpRegNo() {
        when(corpMastManualRepository.findByCorpRegNo("1234567890123")).thenReturn(Optional.of(testCorpMast));

        CorpMastManualResponse response = corpMastManualService.getByCorpRegNo("1234-5678-90123");

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("도시 목록 조회")
    void getAllCities() {
        when(corpMastManualRepository.findDistinctCities()).thenReturn(Arrays.asList("서울특별시", "부산광역시"));

        List<String> cities = corpMastManualService.getAllCities();

        assertThat(cities).hasSize(2);
    }

    @Test
    @DisplayName("도시별 구/군 목록 조회")
    void getDistrictsByCity() {
        when(corpMastManualRepository.findDistinctDistrictsByCity("서울특별시"))
                .thenReturn(Arrays.asList("강남구", "강동구"));

        List<String> districts = corpMastManualService.getDistrictsByCity("서울특별시");

        assertThat(districts).hasSize(2);
    }

    @Test
    @DisplayName("검색 통계 조회")
    void getSearchStatistics() {
        when(corpMastManualRepository.countBySearchConditions(any(), any(), any(), any(), any(), any()))
                .thenReturn(10L);

        var statistics = corpMastManualService.getSearchStatistics(request);

        assertThat(statistics).isNotNull();
        assertThat(statistics.get("totalCount")).isEqualTo(10L);
    }
}

