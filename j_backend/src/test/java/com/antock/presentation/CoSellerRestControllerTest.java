package com.antock.presentation;

import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.presentation.CoSellerRestController;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.config.MockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoSellerRestController.class)
@Import(MockConfig.class)
public class CoSellerRestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CoSellerService coSellerService;
    @Test
    @DisplayName("Endpoint 에 요청")
    void saveCoSeller_success() throws Exception {
        // given
        RegionRequestDto requestDto = RegionRequestDto.builder()
                .city(City.서울특별시)
                .district(District.강남구)
                .build();

        String requestJson = new ObjectMapper().writeValueAsString(requestDto);

        when(coSellerService.saveCoSeller(any())).thenReturn(1);

        // when & then
        mockMvc.perform(post("/coseller/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1))
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andDo(print());

        verify(coSellerService, times(1)).saveCoSeller(any());
    }
}
