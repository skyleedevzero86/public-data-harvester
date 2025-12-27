package com.antock.api.corpmanual.presentation;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import com.antock.api.corpmanual.application.service.CorpMastManualExcelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CorpMastManualApiController.class)
@DisplayName("CorpMastManualApiController 테스트")
class CorpMastManualApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CorpMastManualService corpMastManualService;

    @MockBean
    private CorpMastManualExcelService excelService;

    @Test
    @DisplayName("법인 정보 검색 성공 - 페이징 없음")
    @WithMockUser
    void search_WithoutPaging() throws Exception {
        CorpMastSearchResponse response = CorpMastSearchResponse.builder()
                .id(1L)
                .bizNm("테스트법인")
                .bizNo("123-45-67890")
                .build();

        List<CorpMastSearchResponse> results = Arrays.asList(response);
        given(corpMastManualService.search(any(CorpMastManualRequest.class))).willReturn(results);

        mockMvc.perform(get("/api/v1/corp/search")
                        .param("bizNm", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].bizNm").value("테스트법인"));
    }

    @Test
    @DisplayName("법인 정보 검색 성공 - 페이징 있음")
    @WithMockUser
    void search_WithPaging() throws Exception {
        CorpMastSearchResponse response = CorpMastSearchResponse.builder()
                .id(1L)
                .bizNm("테스트법인")
                .build();

        Page<CorpMastSearchResponse> page = new PageImpl<>(
                Arrays.asList(response),
                PageRequest.of(0, 10),
                1
        );

        given(corpMastManualService.searchWithPagination(any(), anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/corp/search")
                        .param("page", "1")
                        .param("size", "10")
                        .param("bizNm", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("법인 정보 상세 조회 성공")
    @WithMockUser
    void getById_Success() throws Exception {
        CorpMastManualResponse response = CorpMastManualResponse.builder()
                .id(1L)
                .bizNm("테스트법인")
                .bizNo("123-45-67890")
                .siNm("서울특별시")
                .sggNm("강남구")
                .build();

        given(corpMastManualService.getById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/corp/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.bizNm").value("테스트법인"));
    }

    @Test
    @DisplayName("사업자번호로 법인 정보 조회 성공")
    @WithMockUser
    void getByBizNo_Success() throws Exception {
        CorpMastManualResponse response = CorpMastManualResponse.builder()
                .id(1L)
                .bizNo("123-45-67890")
                .bizNm("테스트법인")
                .build();

        given(corpMastManualService.getByBizNo("123-45-67890")).willReturn(response);

        mockMvc.perform(get("/api/v1/corp/bizno/123-45-67890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bizNo").value("123-45-67890"));
    }

    @Test
    @DisplayName("법인등록번호로 법인 정보 조회 성공")
    @WithMockUser
    void getByCorpRegNo_Success() throws Exception {
        CorpMastManualResponse response = CorpMastManualResponse.builder()
                .id(1L)
                .corpRegNo("110111-1234567")
                .bizNm("테스트법인")
                .build();

        given(corpMastManualService.getByCorpRegNo("110111-1234567")).willReturn(response);

        mockMvc.perform(get("/api/v1/corp/regno/110111-1234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.corpRegNo").value("110111-1234567"));
    }

    @Test
    @DisplayName("시/도 목록 조회 성공")
    @WithMockUser
    void getCities_Success() throws Exception {
        List<String> cities = Arrays.asList("서울특별시", "경기도", "부산광역시");
        given(corpMastManualService.getAllCities()).willReturn(cities);

        mockMvc.perform(get("/api/v1/corp/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("구/군 목록 조회 성공")
    @WithMockUser
    void getDistrictsByCity_Success() throws Exception {
        List<String> districts = Arrays.asList("강남구", "서초구", "송파구");
        given(corpMastManualService.getDistrictsByCity("서울특별시")).willReturn(districts);

        mockMvc.perform(get("/api/v1/corp/districts/서울특별시"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("법인 검색 통계 정보 조회 성공")
    @WithMockUser
    void getSearchStatistics_Success() throws Exception {
        Map<String, Object> statistics = Map.of(
                "totalCount", 1000,
                "validCorpRegNoCount", 950,
                "validRegionCdCount", 980
        );

        given(corpMastManualService.getSearchStatistics(any(CorpMastManualRequest.class)))
                .willReturn(statistics);

        mockMvc.perform(get("/api/v1/corp/statistics")
                        .param("bizNm", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1000));
    }

    @Test
    @DisplayName("Excel 내보내기 성공")
    @WithMockUser
    void exportToExcel_Success() throws Exception {
        doNothing().when(excelService).exportToExcel(any(), any());

        mockMvc.perform(get("/api/v1/corp/export")
                        .param("bizNm", "테스트"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().exists("Content-Disposition"));
    }
}

