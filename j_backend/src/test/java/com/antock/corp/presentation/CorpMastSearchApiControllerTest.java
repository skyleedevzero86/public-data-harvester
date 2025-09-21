package com.antock.corp.presentation;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import com.antock.api.corpmanual.presentation.CorpMastManualApiController;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.antock.global.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorpMastApiController 테스트")
@TestPropertySource(properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
public class CorpMastSearchApiControllerTest {

        private ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private CorpMastManualService corpMastSearchService;

        @Mock
        private com.antock.api.member.application.service.AuthTokenService authTokenService;

        @InjectMocks
        private CorpMastManualApiController corpMastSearchApiController;

        private MockMvc standaloneMockMvc;
        private CorpMastManualResponse testCorpResponse;
        private List<CorpMastManualResponse> testCorpList;
        private Page<CorpMastManualResponse> testCorpPage;

        @BeforeEach
        void setUp() {
                standaloneMockMvc = MockMvcBuilders
                                .standaloneSetup(corpMastSearchApiController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                testCorpResponse = CorpMastManualResponse.builder()
                                .id(1L)
                                .sellerId("2025-서울강남-01714")
                                .bizNm("주식회사 뮤직턴")
                                .bizNo("140-81-99474")
                                .corpRegNo("1101110918053")
                                .regionCd("1168010100")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .username("admin")
                                .build();

                CorpMastManualResponse testCorpResponse2 = CorpMastManualResponse.builder()
                                .id(2L)
                                .sellerId("2025-서울강남-01726")
                                .bizNm("주식회사 뷰타민")
                                .bizNo("510-86-03231")
                                .corpRegNo("1101110932733")
                                .regionCd("1168010100")
                                .siNm("서울특별시")
                                .sggNm("강남구")
                                .username("admin")
                                .build();

                testCorpList = Arrays.asList(testCorpResponse, testCorpResponse2);
                testCorpPage = new PageImpl<>(testCorpList, PageRequest.of(0, 20), 2);
        }

        @Test
        @DisplayName("법인 검색 API - 성공")
        void searchApi_WithValidRequest_ShouldReturnSuccessResponse() throws Exception {
                // given
                given(corpMastSearchService.search(any(CorpMastManualRequest.class), any(Pageable.class)))
                                .willReturn(testCorpPage);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("bizNm", "뮤직턴")
                                .param("page", "0")
                                .param("size", "20")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.resultMsg").value("OK"))
                                .andExpect(jsonPath("$.data.content").isArray())
                                .andExpect(jsonPath("$.data.content").isNotEmpty())
                                .andExpect(jsonPath("$.data.totalElements").value(2))
                                .andExpect(jsonPath("$.data.content[0].bizNm").value("주식회사 뮤직턴"))
                                .andExpect(jsonPath("$.data.content[0].bizNo").value("140-81-99474"));
        }

        @Test
        @DisplayName("법인 검색 API - 빈 결과")
        void searchApi_WithNoResults_ShouldReturnEmptyPage() throws Exception {
                // given
                Page<CorpMastManualResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
                given(corpMastSearchService.search(any(CorpMastManualRequest.class), any(Pageable.class)))
                                .willReturn(emptyPage);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("bizNm", "존재하지않는법인")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data.content").isEmpty())
                                .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("법인 검색 API - 복합 조건")
        void searchApi_WithMultipleConditions_ShouldWork() throws Exception {
                // given
                given(corpMastSearchService.search(any(CorpMastManualRequest.class), any(Pageable.class)))
                                .willReturn(testCorpPage);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("bizNm", "주식회사")
                                .param("city", "서울특별시")
                                .param("district", "강남구")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data.content").isArray());

                verify(corpMastSearchService).search(argThat(request -> "주식회사".equals(request.getBizNm()) &&
                                "서울특별시".equals(request.getCity()) &&
                                "강남구".equals(request.getDistrict()) &&
                                request.getPage() == 0 &&
                                request.getSize() == 10));
        }

        @Test
        @DisplayName("법인 상세 조회 API - 성공")
        void getByIdApi_WithValidId_ShouldReturnCorp() throws Exception {
                // given
                Long corpId = 1L;
                given(corpMastSearchService.getById(corpId)).willReturn(testCorpResponse);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/{id}", corpId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.bizNm").value("주식회사 뮤직턴"))
                                .andExpect(jsonPath("$.data.bizNo").value("140-81-99474"))
                                .andExpect(jsonPath("$.data.siNm").value("서울특별시"))
                                .andExpect(jsonPath("$.data.sggNm").value("강남구"));
        }

        @Test
        @DisplayName("사업자번호로 조회 API - 성공")
        void getByBizNoApi_WithValidBizNo_ShouldReturnCorp() throws Exception {
                // given
                String bizNo = "140-81-99474";
                given(corpMastSearchService.getByBizNo(bizNo)).willReturn(testCorpResponse);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/bizno/{bizNo}", bizNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data.bizNo").value("140-81-99474"))
                                .andExpect(jsonPath("$.data.bizNm").value("주식회사 뮤직턴"));
        }

        @Test
        @DisplayName("법인등록번호로 조회 API - 성공")
        void getByCorpRegNoApi_WithValidCorpRegNo_ShouldReturnCorp() throws Exception {
                // given
                String corpRegNo = "1101110918053";
                given(corpMastSearchService.getByCorpRegNo(corpRegNo)).willReturn(testCorpResponse);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/regno/{corpRegNo}", corpRegNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data.corpRegNo").value("1101110918053"))
                                .andExpect(jsonPath("$.data.bizNm").value("주식회사 뮤직턴"));
        }

        @Test
        @DisplayName("검색 통계 API")
        void getSearchStatisticsApi_ShouldReturnStatistics() throws Exception {
                // given
                Map<String, Object> statistics = Map.of(
                                "totalCount", 100L,
                                "locationStats", Map.of("서울특별시", 50L, "부산광역시", 30L));
                given(corpMastSearchService.getSearchStatistics(any(CorpMastManualRequest.class)))
                                .willReturn(statistics);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/statistics")
                                .param("bizNm", "주식회사")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.resultMsg").value("OK"))
                                .andExpect(jsonPath("$.data.totalCount").value(100))
                                .andExpect(jsonPath("$.data.locationStats").exists());
        }

        @Test
        @DisplayName("검색 API - 페이징 경계값 테스트")
        void searchApi_WithBoundaryPageValues_ShouldHandleCorrectly() throws Exception {
                // given
                given(corpMastSearchService.search(any(CorpMastManualRequest.class), any(Pageable.class)))
                                .willReturn(testCorpPage);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("page", "0")
                                .param("size", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200));

                verify(corpMastSearchService)
                                .search(argThat(request -> request.getPage() == 0 && request.getSize() == 1));
        }

        @Test
        @DisplayName("검색 API - 정렬 파라미터")
        void searchApi_WithSortParameter_ShouldWork() throws Exception {
                // given
                given(corpMastSearchService.search(any(CorpMastManualRequest.class), any(Pageable.class)))
                                .willReturn(testCorpPage);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("bizNm", "테스트")
                                .param("sort", "bizNm,asc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200));

                verify(corpMastSearchService).search(argThat(request -> "bizNm,asc".equals(request.getSort())));
        }

        @Test
        @DisplayName("시/도 목록 API - 빈 결과")
        void getCitiesApi_WithEmptyResult_ShouldReturnEmptyArray() throws Exception {
                // given
                given(corpMastSearchService.getAllCities()).willReturn(List.of());

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/cities")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("구/군 목록 API - 존재하지 않는 시/도")
        void getDistrictsByCityApi_WithInvalidCity_ShouldReturnEmptyArray() throws Exception {
                // given
                String invalidCity = "존재하지않는시";
                given(corpMastSearchService.getDistrictsByCity(invalidCity)).willReturn(List.of());

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/districts/{city}", invalidCity)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("검색 통계 API - 조건 없음")
        void getSearchStatisticsApi_WithoutConditions_ShouldWork() throws Exception {
                // given
                Map<String, Object> statistics = Map.of(
                                "totalCount", 0L,
                                "locationStats", Map.of());
                given(corpMastSearchService.getSearchStatistics(any(CorpMastManualRequest.class)))
                                .willReturn(statistics);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data.totalCount").value(0))
                                .andExpect(jsonPath("$.data.locationStats").exists());
        }

        @Test
        @DisplayName("검색 API - 응답 시간 검증")
        void searchApi_ResponseTime_ShouldBeReasonable() throws Exception {
                // given
                given(corpMastSearchService.search(any(CorpMastManualRequest.class), any(Pageable.class)))
                                .willReturn(testCorpPage);

                // when
                long startTime = System.currentTimeMillis();
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("bizNm", "테스트")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk());

                // then
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                assert responseTime < 1000 : "API 응답 시간이 너무 깁니다: " + responseTime + "ms";
        }

        @Test
        @DisplayName("사업자번호 조회 API - URL 인코딩 처리")
        void getByBizNoApi_WithEncodedBizNo_ShouldWork() throws Exception {
                // given
                String bizNo = "140-81-99474";
                String encodedBizNo = "140%2D81%2D99474";
                given(corpMastSearchService.getByBizNo(bizNo)).willReturn(testCorpResponse);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/bizno/{bizNo}", encodedBizNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200));
        }

        @Test
        @DisplayName("사업자번호로 조회 API - 존재하지 않는 사업자번호")
        void getByBizNoApi_WithInvalidBizNo_ShouldReturnError() throws Exception {
                // given
                String invalidBizNo = "000-00-00000";
                given(corpMastSearchService.getByBizNo(invalidBizNo))
                                .willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                                                "해당 사업자번호의 법인정보를 찾을 수 없습니다."));

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/bizno/{bizNo}", invalidBizNo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.resultCode").value(404))
                                .andExpect(jsonPath("$.resultMsg").value("Not Found"))
                                .andExpect(jsonPath("$.errorCode").value("C005"))
                                .andExpect(jsonPath("$.errorMessage").value("해당 사업자번호의 법인정보를 찾을 수 없습니다."));

        }

        @Test
        @DisplayName("시/도 목록 조회 API")
        void getCitiesApi_ShouldReturnCityList() throws Exception {
                // given
                List<String> cities = Arrays.asList("서울특별시", "부산광역시", "대구광역시");
                given(corpMastSearchService.getAllCities()).willReturn(cities);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/cities")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data").value(cities));
        }

        @Test
        @DisplayName("구/군 목록 조회 API")
        void getDistrictsByCityApi_WithValidCity_ShouldReturnDistrictList() throws Exception {
                // given
                String city = "서울특별시";
                List<String> districts = Arrays.asList("강남구", "강북구", "강서구");
                given(corpMastSearchService.getDistrictsByCity(city)).willReturn(districts);

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/districts/{city}", city)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.resultCode").value(200))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data").value(districts));
        }

        @Test
        @DisplayName("법인 상세 조회 API - 존재하지 않는 ID")
        void getByIdApi_WithInvalidId_ShouldReturnError() throws Exception {
                // given
                Long invalidId = 999L;
                given(corpMastSearchService.getById(invalidId))
                                .willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "법인정보를 찾을 수 없습니다."));

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/{id}", invalidId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.resultCode").value(404))
                                .andExpect(jsonPath("$.resultMsg").value("요청한 데이터를 찾을 수 없습니다. - 법인정보를 찾을 수 없습니다."))
                                .andExpect(jsonPath("$.errorCode").value("C005"))
                                .andExpect(jsonPath("$.errorMessage").doesNotExist());
        }

        @Test
        @DisplayName("법인 상세 조회 API - 잘못된 ID 타입")
        void getByIdApi_WithInvalidIdType_ShouldReturnBadRequest() throws Exception {
                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/{id}", "invalid-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.resultCode").value(400))
                                .andExpect(jsonPath("$.resultMsg").value("Bad Request"))
                                .andExpect(jsonPath("$.errorMessage").exists());
        }

        @Test
        @DisplayName("검색 API - 잘못된 파라미터 타입")
        void searchApi_WithInvalidParameterType_ShouldReturnBadRequest() throws Exception {
                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("page", "invalid")
                                .param("size", "invalid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.resultCode").value(400))
                                .andExpect(jsonPath("$.resultMsg").value("Bad Request"))
                                .andExpect(jsonPath("$.errorMessage").exists());
        }

        @Test
        @DisplayName("검색 API - 서비스 예외 처리")
        void searchApi_WithServiceException_ShouldReturnErrorResponse() throws Exception {
                // given
                given(corpMastSearchService.search(any(CorpMastManualRequest.class)))
                                .willThrow(new BusinessException(ErrorCode.CORP_SEARCH_ERROR, "검색 중 오류 발생"));

                // when & then
                standaloneMockMvc.perform(get("/api/v1/corp/search")
                                .param("bizNm", "테스트")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.resultCode").value(500))
                                .andExpect(jsonPath("$.resultMsg").value("Internal Server Error"))
                                .andExpect(jsonPath("$.errorCode").value("CP005"))
                                .andExpect(jsonPath("$.errorMessage").value("검색 중 오류 발생"));
        }

}