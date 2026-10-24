package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.RegionRequestDto;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoSellerManualHistoryTest {

    @Mock
    private CoSellerStorageService storageService;
    @Mock
    private CoSellerApiService apiService;
    @Mock
    private CsvService csvService;
    @Mock
    private CoSellerChunkWriter chunkWriter;
    @Mock
    private FailedChunkRecorder failedChunkRecorder;
    @Mock
    private ManualRegistrationHistory manualRegistrationHistory;

    @InjectMocks
    private CoSellerService coSellerService;

    @Test
    @DisplayName("수동 등록이 끝나면 지역과 등록자 이력을 남긴다")
    void saveCoSeller_recordsHistory() {
        RegionRequestDto request = RegionRequestDto.builder()
                .city(City.서울특별시)
                .district(District.강서구)
                .build();

        coSellerService.saveCoSeller(request, "admin");

        verify(manualRegistrationHistory).record(
                eq("admin"), eq("서울특별시"), eq("강서구"), eq("서울특별시_강서구.csv"), eq(0), isNull());
    }

    @Test
    @DisplayName("파일 처리가 실패해도 실패 이력을 남기고 예외를 다시 던진다")
    void saveCoSeller_recordsFailure() {
        doThrow(new IllegalStateException("파일을 찾을 수 없습니다"))
                .when(csvService).readInChunks(anyString(), anyInt(), any());
        RegionRequestDto request = RegionRequestDto.builder()
                .city(City.서울특별시)
                .district(District.강서구)
                .build();

        assertThatThrownBy(() -> coSellerService.saveCoSeller(request, "admin"))
                .isInstanceOf(IllegalStateException.class);

        verify(manualRegistrationHistory).record(
                eq("admin"), eq("서울특별시"), eq("강서구"), eq("서울특별시_강서구.csv"), eq(0), any(IllegalStateException.class));
    }
}
