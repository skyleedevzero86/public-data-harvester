package com.antock.api.dashboard.presentation;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.dashboard.application.service.RegionStatService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegionStatApiController.class)
@DisplayName("RegionStatApiController 테스트")
class RegionStatApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegionStatService regionStatService;

    @MockBean
    private CorpMastRepository corpMastRepository;

    @Test
    @DisplayName("상위 지역 통계 조회 성공")
    @WithMockUser
    void getTopRegionStat_Success() throws Exception {
        RegionStatDto dto = RegionStatDto.builder()
                .city("서울특별시")
                .district("강남구")
                .totalCount(1000L)
                .completionRate(95.5)
                .build();

        given(regionStatService.getTopRegionStat()).willReturn(dto);

        mockMvc.perform(get("/api/v1/region-stats/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.city").value("서울특별시"));
    }

    @Test
    @DisplayName("전체 지역 통계 조회 성공")
    @WithMockUser
    void getAllRegionStats_Success() throws Exception {
        RegionStatDto dto1 = RegionStatDto.builder()
                .city("서울특별시")
                .district("강남구")
                .totalCount(1000L)
                .build();
        RegionStatDto dto2 = RegionStatDto.builder()
                .city("서울특별시")
                .district("서초구")
                .totalCount(800L)
                .build();

        List<RegionStatDto> stats = Arrays.asList(dto1, dto2);
        given(regionStatService.getAllRegionStats()).willReturn(stats);

        mockMvc.perform(get("/api/v1/region-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("페이징된 지역 통계 조회 성공")
    @WithMockUser
    void getRegionStatsWithPaging_Success() throws Exception {
        RegionStatDto dto = RegionStatDto.builder()
                .city("서울특별시")
                .district("강남구")
                .totalCount(1000L)
                .build();

        Page<RegionStatDto> page = new PageImpl<>(
                Arrays.asList(dto),
                PageRequest.of(0, 25),
                1
        );

        given(regionStatService.getRegionStatsWithPaging(any(), anyString(), anyString()))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/region-stats/paged")
                        .param("page", "0")
                        .param("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("시/도 목록 조회 성공")
    @WithMockUser
    void getCities_Success() throws Exception {
        List<String> cities = Arrays.asList("서울특별시", "경기도", "부산광역시");
        given(regionStatService.getCities()).willReturn(cities);

        mockMvc.perform(get("/api/v1/region-stats/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("구/군 목록 조회 성공")
    @WithMockUser
    void getDistricts_Success() throws Exception {
        List<String> districts = Arrays.asList("강남구", "서초구", "송파구");
        given(regionStatService.getDistrictsByCity("서울특별시")).willReturn(districts);

        mockMvc.perform(get("/api/v1/region-stats/districts")
                        .param("city", "서울특별시"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("지역 상세 정보 조회 성공")
    @WithMockUser
    void getRegionDetails_Success() throws Exception {
        CorpMast corp = CorpMast.builder()
                .id(1L)
                .bizNm("테스트법인")
                .siNm("서울특별시")
                .sggNm("강남구")
                .build();

        Page<CorpMast> page = new PageImpl<>(
                Arrays.asList(corp),
                PageRequest.of(0, 18),
                1
        );

        given(corpMastRepository.findBySiNmAndSggNm(anyString(), anyString(), any()))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/region-stats/details")
                        .param("city", "서울특별시")
                        .param("district", "강남구")
                        .param("page", "0")
                        .param("size", "18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Excel 내보내기 성공")
    @WithMockUser
    void exportToExcel_Success() throws Exception {
        byte[] excelData = "test excel data".getBytes();
        given(regionStatService.exportToExcel(anyString(), anyString())).willReturn(excelData);

        mockMvc.perform(get("/api/v1/region-stats/export")
                        .param("city", "서울특별시"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().exists("Content-Disposition"));
    }
}

