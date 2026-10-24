package com.antock.api.coseller.presentation;

import com.antock.api.coseller.application.service.CoSellerService;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.dashboard.application.service.RegionStatService;
import com.antock.api.dashboard.presentation.RegionStatApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("삭제된 디버그 엔드포인트")
class DebugEndpointRemovedTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CoSellerRestController(mock(CoSellerService.class)),
                        new RegionStatApiController(mock(RegionStatService.class), mock(CorpMastRepository.class)))
                .build();
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "GET, /coseller/debug/minio-files",
            "GET, /coseller/debug/test-file-read",
            "GET, /coseller/debug/minio-file-list",
            "GET, /coseller/debug/test-insert?city=서울특별시&district=강남구",
            "DELETE, /coseller/debug/clear-data",
            "GET, /api/v1/region-stats/debug/fields"
    })
    @DisplayName("디버그 경로는 404")
    void removedDebugPathReturnsNotFound(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path))
                .andExpect(status().isNotFound());
    }
}
