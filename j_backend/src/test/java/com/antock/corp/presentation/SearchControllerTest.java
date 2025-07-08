package com.antock.corp.presentation;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpsearch.application.service.CorpMastSearchService;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.antock.global.config.TestExceptionHandler;
import com.antock.web.corpsearch.presentation.CorpMastSearchWebController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;
import org.mockito.Mockito;

@WebMvcTest(CorpMastSearchWebController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestExceptionHandler.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("CorpMastWebController 테스트")
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorpMastSearchService corpMastService;

    @Configuration
    static class TestConfig {
        @Bean
        public CorpMastSearchService corpMastSearchService() {
            return Mockito.mock(CorpMastSearchService.class);
        }

        @Bean
        public CorpMastSearchWebController corpMastSearchWebController(CorpMastSearchService corpMastSearchService) {
            return new CorpMastSearchWebController(corpMastSearchService);
        }
    }

    private CorpMastSearchResponse testCorpResponse;
    private List<CorpMastSearchResponse> testCorpList;
    private Page<CorpMastSearchResponse> testCorpPage;

    @BeforeEach
    void setUp() {
        testCorpResponse = CorpMastSearchResponse.builder()
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

        CorpMastSearchResponse testCorpResponse2 = CorpMastSearchResponse.builder()
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

        Mockito.reset(corpMastService);
    }

    @Test
    @DisplayName("검색 페이지 - 검색 조건 없이 접근")
    void searchPage_WithoutCondition_ShouldReturnSearchPage() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시", "부산광역시");
        given(corpMastService.getAllCities()).willReturn(cities);

        // when & then
        mockMvc.perform(get("/corp/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"))
                .andExpect(model().attributeExists("searchRequest"))
                .andExpect(model().attributeExists("cities"))
                .andExpect(model().attribute("cities", cities))
                .andExpect(model().attribute("corpList", nullValue()));
    }

    @Test
    @DisplayName("검색 페이지 - 법인명으로 검색")
    void searchPage_WithBizNm_ShouldReturnSearchResults() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시", "부산광역시");
        Map<String, Object> statistics = Map.of("totalCount", 2L, "locationStats", Map.of());

        given(corpMastService.getAllCities()).willReturn(cities);
        given(corpMastService.search(any(CorpMastSearchRequest.class))).willReturn(testCorpPage);
        given(corpMastService.getSearchStatistics(any(CorpMastSearchRequest.class))).willReturn(statistics);

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("bizNm", "뮤직턴"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"))
                .andExpect(model().attributeExists("searchRequest"))
                .andExpect(model().attributeExists("corpList"))
                .andExpect(model().attributeExists("statistics"))
                .andExpect(model().attribute("cities", cities));
    }

    @Test
    @DisplayName("검색 페이지 - 지역으로 검색")
    void searchPage_WithLocation_ShouldReturnSearchResults() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시", "부산광역시");
        List<String> districts = Arrays.asList("강남구", "강북구");
        Map<String, Object> statistics = Map.of("totalCount", 2L, "locationStats", Map.of());

        given(corpMastService.getAllCities()).willReturn(cities);
        given(corpMastService.getDistrictsByCity("서울특별시")).willReturn(districts);
        given(corpMastService.search(any(CorpMastSearchRequest.class))).willReturn(testCorpPage);
        given(corpMastService.getSearchStatistics(any(CorpMastSearchRequest.class))).willReturn(statistics);

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("city", "서울특별시")
                        .param("district", "강남구"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"))
                .andExpect(model().attributeExists("searchRequest"))
                .andExpect(model().attributeExists("corpList"))
                .andExpect(model().attributeExists("districts"))
                .andExpect(model().attribute("districts", districts));
    }

    @Test
    @DisplayName("검색 페이지 - 페이징 파라미터")
    void searchPage_WithPagingParams_ShouldHandleCorrectly() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시");
        Map<String, Object> statistics = Map.of("totalCount", 2L, "locationStats", Map.of());

        given(corpMastService.getAllCities()).willReturn(cities);
        given(corpMastService.search(any(CorpMastSearchRequest.class))).willReturn(testCorpPage);
        given(corpMastService.getSearchStatistics(any(CorpMastSearchRequest.class))).willReturn(statistics);

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("bizNm", "테스트")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"));

        verify(corpMastService).search(argThat(request ->
                request.getPage() == 1 && request.getSize() == 10));
    }

    @Test
    @DisplayName("법인 상세 페이지 - 정상 조회")
    void detailPage_WithValidId_ShouldReturnDetailPage() throws Exception {
        // given
        Long corpId = 1L;
        given(corpMastService.getById(corpId)).willReturn(testCorpResponse);

        // when & then
        mockMvc.perform(get("/corp/detail/{id}", corpId))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/detail"))
                .andExpect(model().attributeExists("corp"))
                .andExpect(model().attribute("corp", testCorpResponse));
    }

    @Test
    @DisplayName("법인 상세 페이지 - 존재하지 않는 ID")
    void detailPage_WithInvalidId_ShouldRedirectToSearch() throws Exception {
        // given
        Long invalidId = 999L;
        String expectedErrorMessage = "법인정보를 찾을 수 없습니다.";
        given(corpMastService.getById(invalidId))
                .willThrow(new BusinessException(ErrorCode.COMMON_ENTITY_NOT_FOUND, expectedErrorMessage));

        // when & then
        mockMvc.perform(get("/corp/detail/{id}", invalidId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/corp/search"));
    }

    @Test
    @DisplayName("AJAX - 구/군 목록 조회")
    void getDistrictsByCity_WithValidCity_ShouldReturnDistrictList() throws Exception {
        // given
        String city = "서울특별시";
        List<String> districts = Arrays.asList("강남구", "강북구", "강서구");
        given(corpMastService.getDistrictsByCity(city)).willReturn(districts);

        // when & then
        mockMvc.perform(get("/corp/districts/{city}", city))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("강남구"))
                .andExpect(jsonPath("$[1]").value("강북구"))
                .andExpect(jsonPath("$[2]").value("강서구"));
    }

    @Test
    @DisplayName("검색 조건 초기화")
    void resetSearch_ShouldRedirectToSearchPage() throws Exception {
        // when & then
        mockMvc.perform(post("/corp/search/reset")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/corp/search"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("Excel 다운로드 요청")
    void exportToExcel_ShouldRedirectWithMessage() throws Exception {
        // when & then
        mockMvc.perform(get("/corp/export")
                        .param("bizNm", "테스트"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/corp/search?*"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("검색 페이지 - 서비스 예외 처리")
    void searchPage_WithServiceException_ShouldHandleGracefully() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시");
        given(corpMastService.getAllCities()).willReturn(cities);
        given(corpMastService.search(any(CorpMastSearchRequest.class)))
                .willThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "검색 중 오류 발생"));

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("bizNm", "테스트")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"))
                .andExpect(model().attributeExists("cities"))
                .andExpect(model().attribute("cities", cities))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "서버 오류가 발생했습니다. - 검색 중 오류 발생"))
                .andExpect(model().attributeExists("searchRequest"));
    }

    @Test
    @DisplayName("사업자번호 형식으로 검색")
    void searchPage_WithFormattedBizNo_ShouldWork() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시");
        Map<String, Object> statistics = Map.of("totalCount", 1L, "locationStats", Map.of());

        given(corpMastService.getAllCities()).willReturn(cities);
        given(corpMastService.search(any(CorpMastSearchRequest.class))).willReturn(testCorpPage);
        given(corpMastService.getSearchStatistics(any(CorpMastSearchRequest.class))).willReturn(statistics);

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("bizNo", "140-81-99474"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"));

        verify(corpMastService).search(argThat(request ->
                "140-81-99474".equals(request.getBizNo())));
    }

    @Test
    @DisplayName("판매자ID로 검색")
    void searchPage_WithSellerId_ShouldWork() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시");
        Map<String, Object> statistics = Map.of("totalCount", 1L, "locationStats", Map.of());

        given(corpMastService.getAllCities()).willReturn(cities);
        given(corpMastService.search(any(CorpMastSearchRequest.class))).willReturn(testCorpPage);
        given(corpMastService.getSearchStatistics(any(CorpMastSearchRequest.class))).willReturn(statistics);

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("sellerId", "2025-서울강남"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"));

        verify(corpMastService).search(argThat(request ->
                "2025-서울강남".equals(request.getSellerIdForSearch())));
    }

    @Test
    @DisplayName("법인등록번호로 검색")
    void searchPage_WithCorpRegNo_ShouldWork() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시");
        Map<String, Object> statistics = Map.of("totalCount", 1L, "locationStats", Map.of());

        given(corpMastService.getAllCities()).willReturn(cities);
        given(corpMastService.search(any(CorpMastSearchRequest.class))).willReturn(testCorpPage);
        given(corpMastService.getSearchStatistics(any(CorpMastSearchRequest.class))).willReturn(statistics);

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("corpRegNo", "1101110918053"))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"));

        verify(corpMastService).search(argThat(request ->
                "1101110918053".equals(request.getCorpRegNoForSearch())));
    }

    @Test
    @DisplayName("빈 검색 조건으로 요청")
    void searchPage_WithEmptyConditions_ShouldNotCallSearch() throws Exception {
        // given
        List<String> cities = Arrays.asList("서울특별시");
        given(corpMastService.getAllCities()).willReturn(cities);

        // when & then
        mockMvc.perform(get("/corp/search")
                        .param("bizNm", "")
                        .param("bizNo", "")
                        .param("city", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("corp/search"))
                .andExpect(model().attribute("corpList", nullValue()));

        verify(corpMastService, never()).search(any());
        verify(corpMastService, never()).getSearchStatistics(any());
    }
}
