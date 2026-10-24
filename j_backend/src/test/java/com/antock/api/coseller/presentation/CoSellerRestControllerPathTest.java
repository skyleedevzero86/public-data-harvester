package com.antock.api.coseller.presentation;

import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.global.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("코셀러 지역 파라미터 검증")
class CoSellerRestControllerPathTest {

    @Test
    @DisplayName("허용되지 않은 city는 400")
    void invalidCityReturnsBadRequest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CoSellerRestController(mock(CoSellerService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/coseller/save-simple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"../../etc\",\"district\":\"강남구\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("입력값이 올바르지 않습니다.")))
                .andExpect(content().string(not(containsString("application.yml"))));
    }
}
