package com.antock.api.coseller.application.service;

import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManualRegistrationHistoryTest {

    @Mock
    private CsvBatchHistoryRepository csvBatchHistoryRepository;

    @InjectMocks
    private ManualRegistrationHistory history;

    @Test
    @DisplayName("저장된 건수가 있으면 성공 이력을 남긴다")
    void record_success() {
        history.record("admin", "서울특별시", "강서구", "서울특별시_강서구.csv", 12, null);

        CsvBatchHistory saved = capture();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getCity()).isEqualTo("서울특별시");
        assertThat(saved.getDistrict()).isEqualTo("강서구");
        assertThat(saved.getStatus()).isEqualTo("SUCCESS");
        assertThat(saved.getMessage()).isEqualTo("성공(12건)");
        assertThat(saved.getRecordCount()).isEqualTo(12);
    }

    @Test
    @DisplayName("예외가 있으면 실패 사유를 이력에 남긴다")
    void record_failure() {
        history.record("admin", "서울특별시", "강서구", "서울특별시_강서구.csv", 0,
                new IllegalStateException("파일을 찾을 수 없습니다"));

        CsvBatchHistory saved = capture();
        assertThat(saved.getStatus()).isEqualTo("FAIL");
        assertThat(saved.getMessage()).isEqualTo("실패(파일을 찾을 수 없습니다)");
    }

    @Test
    @DisplayName("등록자가 있는 최근 이력만 화면 행으로 바꾼다")
    void recent_mapsRows() {
        when(csvBatchHistoryRepository.findTop20ByUsernameIsNotNullOrderByTimestampDesc())
                .thenReturn(List.of(CsvBatchHistory.builder()
                        .username("admin")
                        .city("서울특별시")
                        .district("강서구")
                        .status("SUCCESS")
                        .message("성공(12건)")
                        .timestamp(LocalDateTime.of(2026, 10, 21, 16, 6, 56))
                        .build()));

        List<ManualRegistrationHistory.Row> rows = history.recent();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getUsername()).isEqualTo("admin");
        assertThat(rows.get(0).getRegisteredAt()).isEqualTo("2026-10-21 16:06:56");
        assertThat(rows.get(0).isSuccess()).isTrue();
    }

    private CsvBatchHistory capture() {
        ArgumentCaptor<CsvBatchHistory> captor = ArgumentCaptor.forClass(CsvBatchHistory.class);
        verify(csvBatchHistoryRepository).save(captor.capture());
        return captor.getValue();
    }
}
