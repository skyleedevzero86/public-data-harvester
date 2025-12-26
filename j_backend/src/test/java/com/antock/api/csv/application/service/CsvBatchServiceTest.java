package com.antock.api.csv.application.service;

import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CorpInfoApiClient;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import com.antock.api.csv.infrastructure.CsvFileWriter;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsvBatchService 테스트")
class CsvBatchServiceTest {

    @Mock
    private CorpInfoApiClient apiClient;

    @Mock
    private CsvFileWriter fileWriter;

    @Mock
    private CsvBatchHistoryRepository historyRepo;

    @InjectMocks
    private CsvBatchService csvBatchService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(csvBatchService, "maxRetryAttempts", 3);
        ReflectionTestUtils.setField(csvBatchService, "retryDelay", 1000L);
        ReflectionTestUtils.setField(csvBatchService, "chunkSize", 1000);
        ReflectionTestUtils.setField(csvBatchService, "maxConcurrent", 5);
    }

    @Test
    @DisplayName("지역 처리 성공")
    void processDistrict_success() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("sellerId", "seller1");
        data.put("bizNm", "테스트 법인");

        when(apiClient.fetchAll(anyString(), anyString())).thenReturn(Arrays.asList(data));
        doNothing().when(fileWriter).writeCsv(anyString(), anyString(), anyList(), anyList(), anyMap());
        when(historyRepo.save(any(CsvBatchHistory.class))).thenReturn(new CsvBatchHistory());

        csvBatchService.processDistrict(City.서울특별시, District.강남구);

        verify(apiClient).fetchAll(anyString(), anyString());
        verify(fileWriter).writeCsv(anyString(), anyString(), anyList(), anyList(), anyMap());
    }

    @Test
    @DisplayName("지역 처리 - 데이터 없음")
    void processDistrict_noData() {
        when(apiClient.fetchAll(anyString(), anyString())).thenReturn(Arrays.asList());
        when(historyRepo.save(any(CsvBatchHistory.class))).thenReturn(new CsvBatchHistory());

        csvBatchService.processDistrict(City.서울특별시, District.강남구);

        verify(historyRepo, atLeastOnce()).save(any(CsvBatchHistory.class));
    }

    @Test
    @DisplayName("지역 처리 - API 호출 실패")
    void processDistrict_apiFailure() {
        when(apiClient.fetchAll(anyString(), anyString())).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> csvBatchService.processDistrict(City.서울특별시, District.강남구))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CORP_SEARCH_ERROR);
    }

    @Test
    @DisplayName("지역 처리 - 파일 작성 실패")
    void processDistrict_fileWriteFailure() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("sellerId", "seller1");

        when(apiClient.fetchAll(anyString(), anyString())).thenReturn(Arrays.asList(data));
        doThrow(new IOException("Write error")).when(fileWriter).writeCsv(anyString(), anyString(), anyList(), anyList(), anyMap());

        assertThatThrownBy(() -> csvBatchService.processDistrict(City.서울특별시, District.강남구))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CORP_SEARCH_ERROR);
    }
}

