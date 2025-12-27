package com.antock.corp.application.service;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
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
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorpMastService 테스트")
class CorpMastServiceTest {

    @InjectMocks
    private CorpMastManualService corpMastService;

    @Mock
    private CorpMastManualRepository corpMastRepository;

    private CorpMast testCorp1;
    private CorpMast testCorp2;
    private List<CorpMast> testCorpList;

    @BeforeEach
    void setUp() {
        testCorp1 = CorpMast.builder()
                .sellerId("2025-서울강남-01714")
                .bizNm("주식회사 뮤직턴")
                .bizNo("140-81-99474")
                .corpRegNo("1101110918053")
                .regionCd("1168010100")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("admin")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testCorp1, "id", 1L);

        testCorp2 = CorpMast.builder()
                .sellerId("2025-서울강남-01726")
                .bizNm("주식회사 뷰타민")
                .bizNo("510-86-03231")
                .corpRegNo("1101110932733")
                .regionCd("1168010100")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("admin")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testCorp2, "id", 2L);

        testCorpList = Arrays.asList(testCorp1, testCorp2);
    }

    @Test
    @DisplayName("검색 조건이 없을 때 전체 조회")
    void search_WithoutCondition_ShouldReturnAllCorps() {
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setPage(0);
        request.setSize(20);

        Page<CorpMast> mockPage = new PageImpl<>(testCorpList, PageRequest.of(0, 20), 2);
        given(corpMastRepository.findBySearchConditions(isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(mockPage);

        Page<CorpMastManualResponse> result = corpMastService.search(request, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getBizNm()).isEqualTo("주식회사 뮤직턴");
        assertThat(result.getContent().get(1).getBizNm()).isEqualTo("주식회사 뷰타민");
    }

    @Test
    @DisplayName("법인명으로 검색")
    void search_WithBizNm_ShouldReturnFilteredResult() {
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setBizNm("뮤직턴");
        request.setPage(0);
        request.setSize(20);

        Page<CorpMast> mockPage = new PageImpl<>(Arrays.asList(testCorp1), PageRequest.of(0, 20), 1);
        given(corpMastRepository.findBySearchConditions(eq("뮤직턴"), isNull(), isNull(),
                isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(mockPage);

        Page<CorpMastManualResponse> result = corpMastService.search(request, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBizNm()).isEqualTo("주식회사 뮤직턴");
    }

    @Test
    @DisplayName("사업자번호로 검색")
    void search_WithBizNo_ShouldReturnFilteredResult() {
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setBizNo("140-81-99474");
        request.setPage(0);
        request.setSize(20);

        Page<CorpMast> mockPage = new PageImpl<>(Arrays.asList(testCorp1), PageRequest.of(0, 20), 1);

        given(corpMastRepository.findBySearchConditions(isNull(), eq("1408199474"), isNull(),
                isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(mockPage);

        Page<CorpMastManualResponse> result = corpMastService.search(request, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBizNo()).isEqualTo("140-81-99474");
    }

    @Test
    @DisplayName("지역으로 검색")
    void search_WithLocation_ShouldReturnFilteredResult() {
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setCity("서울특별시");
        request.setDistrict("강남구");
        request.setPage(0);
        request.setSize(20);

        Page<CorpMast> mockPage = new PageImpl<>(testCorpList, PageRequest.of(0, 20), 2);
        given(corpMastRepository.findBySearchConditions(isNull(), isNull(), isNull(),
                isNull(), eq("서울특별시"), eq("강남구"), any(Pageable.class)))
                .willReturn(mockPage);

        Page<CorpMastManualResponse> result = corpMastService.search(request, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(corp -> corp.getSiNm().equals("서울특별시") && corp.getSggNm().equals("강남구"));
    }

    @Test
    @DisplayName("ID로 법인 조회 성공")
    void getById_WithValidId_ShouldReturnCorp() {
        Long corpId = 1L;
        given(corpMastRepository.findById(corpId)).willReturn(Optional.of(testCorp1));

        CorpMastManualResponse result = corpMastService.getById(corpId);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBizNm()).isEqualTo("주식회사 뮤직턴");
        assertThat(result.getBizNo()).isEqualTo("140-81-99474");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 법인 조회 시 예외 발생")
    void getById_WithInvalidId_ShouldThrowException() {
        Long invalidId = 999L;
        given(corpMastRepository.findById(invalidId)).willReturn(Optional.empty());

 & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> corpMastService.getById(invalidId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        assertThat(exception.getMessage()).contains("법인정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사업자번호로 법인 조회 성공")
    void getByBizNo_WithValidBizNo_ShouldReturnCorp() {
        String bizNo = "140-81-99474";
        given(corpMastRepository.findByBizNo(bizNo)).willReturn(Optional.of(testCorp1));

        CorpMastManualResponse result = corpMastService.getByBizNo(bizNo);

        assertThat(result.getBizNo()).isEqualTo("140-81-99474");
        assertThat(result.getBizNm()).isEqualTo("주식회사 뮤직턴");
    }

    @Test
    @DisplayName("존재하지 않는 사업자번호로 조회 시 예외 발생")
    void getByBizNo_WithInvalidBizNo_ShouldThrowException() {
        String invalidBizNo = "000-00-00000";
        given(corpMastRepository.findByBizNo(invalidBizNo)).willReturn(Optional.empty());

 & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> corpMastService.getByBizNo(invalidBizNo));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        assertThat(exception.getMessage()).contains("해당 사업자번호의 법인정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("법인등록번호로 법인 조회 성공")
    void getByCorpRegNo_WithValidCorpRegNo_ShouldReturnCorp() {
        String corpRegNo = "1101110918053";
        given(corpMastRepository.findByCorpRegNo(corpRegNo)).willReturn(Optional.of(testCorp1));

        CorpMastManualResponse result = corpMastService.getByCorpRegNo(corpRegNo);

        assertThat(result.getCorpRegNo()).isEqualTo("1101110918053");
        assertThat(result.getBizNm()).isEqualTo("주식회사 뮤직턴");
    }

    @Test
    @DisplayName("시/도 목록 조회")
    void getAllCities_ShouldReturnCityList() {
        List<String> cities = Arrays.asList("서울특별시", "부산광역시", "대구광역시");
        given(corpMastRepository.findDistinctCities()).willReturn(cities);

        List<String> result = corpMastService.getAllCities();

        assertThat(result).hasSize(3);
        assertThat(result).contains("서울특별시", "부산광역시", "대구광역시");
    }

    @Test
    @DisplayName("특정 시/도의 구/군 목록 조회")
    void getDistrictsByCity_WithValidCity_ShouldReturnDistrictList() {
        String city = "서울특별시";
        List<String> districts = Arrays.asList("강남구", "강북구", "강서구");
        given(corpMastRepository.findDistinctDistrictsByCity(city)).willReturn(districts);

        List<String> result = corpMastService.getDistrictsByCity(city);

        assertThat(result).hasSize(3);
        assertThat(result).contains("강남구", "강북구", "강서구");
    }

    @Test
    @DisplayName("빈 시/도로 구/군 조회 시 빈 리스트 반환")
    void getDistrictsByCity_WithEmptyCity_ShouldReturnEmptyList() {
        String emptyCity = "";

        List<String> result = corpMastService.getDistrictsByCity(emptyCity);

        assertThat(result).isEmpty();
        verify(corpMastRepository, never()).findDistinctDistrictsByCity(any());
    }

    @Test
    @DisplayName("검색 통계 정보 조회")
    void getSearchStatistics_WithSearchCondition_ShouldReturnStats() {
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setBizNm("테스트");

        Page<CorpMast> mockPage = new PageImpl<>(testCorpList, PageRequest.of(0, 1), 2);
        given(corpMastRepository.findBySearchConditions(eq("테스트"), isNull(), isNull(),
                isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(mockPage);

        var result = corpMastService.getSearchStatistics(request);

        assertThat(result.get("totalCount")).isEqualTo(2L);
        assertThat(result.get("locationStats")).isNotNull();
    }

    @Test
    @DisplayName("페이지 크기 제한 테스트")
    void search_WithLargePageSize_ShouldLimitPageSize() {
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setSize(200); // 제한을 초과하는 크기
        request.setPage(0);

        Page<CorpMast> mockPage = new PageImpl<>(testCorpList, PageRequest.of(0, 100), 2);
        given(corpMastRepository.findBySearchConditions(any(), any(), any(),
                any(), any(), any(), any(Pageable.class)))
                .willReturn(mockPage);

        corpMastService.search(request);

        verify(corpMastRepository).findBySearchConditions(any(), any(), any(),
                any(), any(), any(), argThat(pageable -> pageable.getPageSize() <= 100));
    }

    @Test
    @DisplayName("음수 페이지 번호 처리")
    void search_WithNegativePage_ShouldUseZero() {
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setPage(-1);
        request.setSize(20);

        Page<CorpMast> mockPage = new PageImpl<>(testCorpList, PageRequest.of(0, 20), 2);
        given(corpMastRepository.findBySearchConditions(any(), any(), any(),
                any(), any(), any(), any(Pageable.class)))
                .willReturn(mockPage);

        corpMastService.search(request);

        verify(corpMastRepository).findBySearchConditions(any(), any(), any(),
                any(), any(), any(), argThat(pageable -> pageable.getPageNumber() == 0));
    }
}