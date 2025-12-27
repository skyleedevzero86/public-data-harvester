package com.antock.api.coseller.presentation;

import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.application.service.CoSellerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoSellerRestController.class)
@DisplayName("CoSellerRestController 테스트")
class CoSellerRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoSellerService cosellerService;

    @Test
    @DisplayName("코셀러 데이터 저장 성공")
    @WithMockUser
    void saveCoSeller_Success() throws Exception {
        RegionRequestDto request = RegionRequestDto.builder()
                .city("서울특별시")
                .district("강남구")
                .build();

        given(cosellerService.saveCoSeller(any(RegionRequestDto.class), anyString())).willReturn(100);

        mockMvc.perform(post("/coseller/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("코셀러 데이터 간단 저장 성공")
    @WithMockUser
    void saveCoSellerSimple_Success() throws Exception {
        Map<String, String> request = Map.of(
                "city", "서울특별시",
                "district", "강남구"
        );

        given(cosellerService.saveCoSeller(anyString(), anyString(), anyString())).willReturn(100);

        mockMvc.perform(post("/coseller/save-simple")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("코셀러 데이터 간단 저장 - 필수 파라미터 누락")
    @WithMockUser
    void saveCoSellerSimple_MissingParams() throws Exception {
        Map<String, String> request = Map.of("city", "서울특별시");

        mockMvc.perform(post("/coseller/save-simple")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("MinIO 파일 목록 조회 성공")
    @WithMockUser
    void getMinioFileList_Success() throws Exception {
        mockMvc.perform(get("/coseller/debug/minio-file-list"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("파일 읽기 테스트 성공")
    @WithMockUser
    void testFileRead_Success() throws Exception {
        mockMvc.perform(get("/coseller/debug/test-file-read")
                        .param("city", "서울특별시")
                        .param("district", "강남구"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("데이터 삽입 테스트 성공")
    @WithMockUser
    void testInsert_Success() throws Exception {
        given(cosellerService.saveCoSeller(anyString(), anyString(), anyString())).willReturn(50);

        mockMvc.perform(get("/coseller/debug/test-insert")
                        .param("city", "서울특별시")
                        .param("district", "강남구"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("데이터 정리 성공")
    @WithMockUser
    void clearData_Success() throws Exception {
        given(cosellerService.clearAllData()).willReturn(100);

        mockMvc.perform(delete("/coseller/debug/clear-data")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}

