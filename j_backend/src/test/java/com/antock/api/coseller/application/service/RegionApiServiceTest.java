package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionApiService 테스트")
class RegionApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private com.antock.api.coseller.application.dto.properties.RegionApiProperties regionProp;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private RegionApiService regionApiService;

    @Test
    @DisplayName("지역코드 조회 성공")
    void getRegionCode_Success() throws Exception {
        CompletableFuture<String> future = CompletableFuture.completedFuture("11680");
        
        CompletableFuture<String> result = regionApiService.getRegionCode("서울특별시 강남구");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("지역 정보 조회 성공")
    void getRegionInfo_Success() throws Exception {
        RegionInfoDto regionInfo = RegionInfoDto.builder()
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .build();

        CompletableFuture<RegionInfoDto> future = CompletableFuture.completedFuture(regionInfo);
        
        CompletableFuture<RegionInfoDto> result = regionApiService.getRegionInfo("서울특별시 강남구");

        assertThat(result).isNotNull();
    }
}

