package com.antock.api.csv.application.service;

import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CorpInfoFetcher;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsvBatchService 테스트")
class CsvBatchServiceTest {

    @Mock
    private CorpInfoFetcher corpInfoFetcher;

    @Mock
    private CsvFileWriter fileWriter;

    @Mock
    private CsvBatchHistoryRepository historyRepo;

    @InjectMocks
    private CsvBatchService csvBatchService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(csvBatchService, "chunkSize", 1000);
        ReflectionTestUtils.setField(csvBatchService, "maxConcurrent", 5);
    }

    @Test
    @DisplayName("지역 처리 성공")
    void processDistrict_success() throws IOException {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("sellerId", "seller1");
        data.put("bizNm", "테스트 법인");
        when(corpInfoFetcher.fetch(anyString(), anyString())).thenReturn(Arrays.asList(data));
        doNothing().when(fileWriter).writeCsv(anyString(), anyString(), anyList(), anyList(), anyMap());
        when(historyRepo.save(any(CsvBatchHistory.class))).thenReturn(new CsvBatchHistory());

        // when
        csvBatchService.processDistrict(City.서울특별시, District.강남구);

        // then
        verify(corpInfoFetcher).fetch(anyString(), anyString());
        verify(fileWriter).writeCsv(anyString(), anyString(), anyList(), anyList(), anyMap());
    }

    @Test
    @DisplayName("지역 처리 - 데이터 없음")
    void processDistrict_noData() {
        // given
        when(corpInfoFetcher.fetch(anyString(), anyString())).thenReturn(Arrays.asList());
        when(historyRepo.save(any(CsvBatchHistory.class))).thenReturn(new CsvBatchHistory());

        // when
        csvBatchService.processDistrict(City.서울특별시, District.강남구);

        // then
        verify(historyRepo, atLeastOnce()).save(any(CsvBatchHistory.class));
    }

    @Test
    @DisplayName("지역 처리 - API 호출 실패")
    void processDistrict_apiFailure() {
        // given
        when(corpInfoFetcher.fetch(anyString(), anyString())).thenThrow(new RuntimeException("API error"));

        // when
        Throwable thrown = catchThrowable(() -> csvBatchService.processDistrict(City.서울특별시, District.강남구));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CORP_SEARCH_ERROR);
    }

    @Test
    @DisplayName("지역 처리 - 파일 작성 실패")
    void processDistrict_fileWriteFailure() throws IOException {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("sellerId", "seller1");
        when(corpInfoFetcher.fetch(anyString(), anyString())).thenReturn(Arrays.asList(data));
        doThrow(new IOException("Write error")).when(fileWriter).writeCsv(anyString(), anyString(), anyList(), anyList(), anyMap());

        // when
        Throwable thrown = catchThrowable(() -> csvBatchService.processDistrict(City.서울특별시, District.강남구));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CORP_SEARCH_ERROR);
    }

    @Test
    @DisplayName("배치는 모든 시·도의 구·군을 처리한다")
    void runBatch_processesEveryCityAndDistrict() {
        // given
        when(corpInfoFetcher.fetch(anyString(), anyString())).thenReturn(List.of());

        // when
        csvBatchService.runBatch();

        // then
        @SuppressWarnings("unchecked")
        Map<City, List<District>> cityDistricts = (Map<City, List<District>>) ReflectionTestUtils
                .getField(CsvBatchService.class, "CITY_DISTRICT_MAP");
        int districtCount = cityDistricts.values().stream().mapToInt(List::size).sum();
        verify(corpInfoFetcher, times(districtCount)).fetch(anyString(), anyString());
        for (City city : cityDistricts.keySet()) {
            verify(corpInfoFetcher, atLeastOnce()).fetch(eq(city.name()), anyString());
        }
    }

    @Test
    @DisplayName("동시에 처리하는 시·도는 maxConcurrent를 넘지 않는다")
    void runBatch_limitsConcurrencyToMaxConcurrent() {
        // given
        ReflectionTestUtils.setField(csvBatchService, "maxConcurrent", 2);
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        when(corpInfoFetcher.fetch(anyString(), anyString())).thenAnswer(invocation -> {
            int current = inFlight.incrementAndGet();
            peak.updateAndGet(max -> Math.max(max, current));
            try {
                Thread.sleep(20);
            } finally {
                inFlight.decrementAndGet();
            }
            return List.of();
        });

        // when
        csvBatchService.runBatch();

        // then
        assertThat(peak.get()).isLessThanOrEqualTo(2);
        assertThat(peak.get()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("지역 처리 실패 시 ERROR 이력을 저장한다")
    void processDistrict_apiFailure_savesErrorHistory() {
        // given
        when(corpInfoFetcher.fetch(anyString(), anyString())).thenThrow(new RuntimeException("API error"));

        // when
        Throwable thrown = catchThrowable(() -> csvBatchService.processDistrict(City.서울특별시, District.강남구));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CORP_SEARCH_ERROR);
        ArgumentCaptor<CsvBatchHistory> saved = ArgumentCaptor.forClass(CsvBatchHistory.class);
        verify(historyRepo).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo("ERROR");
    }
}

