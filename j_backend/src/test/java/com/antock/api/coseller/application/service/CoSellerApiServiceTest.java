package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoSellerApiService 테스트")
class CoSellerApiServiceTest {

    @Mock
    private CorpApiClient corpApiClient;

    @Mock
    private RegionApiClient regionApiClient;

    @Mock
    private CoSellerDataMapper dataMapper;

    @InjectMocks
    private CoSellerApiService coSellerApiService;

    private BizCsvInfoDto csvInfo;
    private RegionInfoDto regionInfo;

    @BeforeEach
    void setUp() {
        csvInfo = BizCsvInfoDto.builder()
                .sellerId("seller1")
                .bizNo("123-45-67890")
                .bizNm("테스트 법인")
                .bizAddress("서울특별시 강남구")
                .build();

        regionInfo = RegionInfoDto.builder()
                .regionCd("11000")
                .siNm("서울특별시")
                .sggNm("강남구")
                .build();
    }

    @Test
    @DisplayName("비동기 처리 성공")
    void processAsync_success() {
        CompletableFuture<String> corpRegNoFuture = CompletableFuture.completedFuture("123456-7890123");
        CompletableFuture<RegionInfoDto> regionInfoFuture = CompletableFuture.completedFuture(regionInfo);
        CorpMastCreateDTO createDTO = CorpMastCreateDTO.builder()
                .bizNo("123-45-67890")
                .bizNm("테스트 법인")
                .build();

        when(corpApiClient.getCorpRegNo("123-45-67890")).thenReturn(corpRegNoFuture);
        when(regionApiClient.getRegionInfo("서울특별시 강남구")).thenReturn(regionInfoFuture);
        when(dataMapper.mapFromApiData(csvInfo, "123456-7890123", regionInfo, "testuser"))
                .thenReturn(Optional.of(createDTO));

        CompletableFuture<Optional<CorpMastCreateDTO>> result = coSellerApiService.processAsync(csvInfo, "testuser");

        assertThat(result).isNotNull();
        Optional<CorpMastCreateDTO> dto = result.join();
        assertThat(dto).isPresent();
    }

    @Test
    @DisplayName("비동기 처리 실패 - 예외 발생")
    void processAsync_exception() {
        when(corpApiClient.getCorpRegNo(anyString())).thenThrow(new RuntimeException("API Error"));

        CompletableFuture<Optional<CorpMastCreateDTO>> result = coSellerApiService.processAsync(csvInfo, "testuser");

        assertThat(result).isNotNull();
        Optional<CorpMastCreateDTO> dto = result.join();
        assertThat(dto).isEmpty();
    }

    @Test
    @DisplayName("비동기 처리 - 데이터 매퍼가 빈 결과 반환")
    void processAsync_emptyResult() {
        CompletableFuture<String> corpRegNoFuture = CompletableFuture.completedFuture("123456-7890123");
        CompletableFuture<RegionInfoDto> regionInfoFuture = CompletableFuture.completedFuture(regionInfo);

        when(corpApiClient.getCorpRegNo("123-45-67890")).thenReturn(corpRegNoFuture);
        when(regionApiClient.getRegionInfo("서울특별시 강남구")).thenReturn(regionInfoFuture);
        when(dataMapper.mapFromApiData(csvInfo, "123456-7890123", regionInfo, "testuser"))
                .thenReturn(Optional.empty());

        CompletableFuture<Optional<CorpMastCreateDTO>> result = coSellerApiService.processAsync(csvInfo, "testuser");

        assertThat(result).isNotNull();
        Optional<CorpMastCreateDTO> dto = result.join();
        assertThat(dto).isEmpty();
    }
}

