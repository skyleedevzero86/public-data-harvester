package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("청크 API 보강")
class CoSellerChunkWriterTest {

    @Mock
    private CoSellerApiService apiService;

    @Mock
    private CoSellerDataMapper dataMapper;

    @Mock
    private CoSellerStorageService storageService;

    @InjectMocks
    private CoSellerChunkWriter chunkWriter;

    @Test
    @DisplayName("한 행의 API 실패는 그 행만 기본 매핑하고 나머지 행은 유지한다")
    void oneApiFailureDoesNotDropTheChunk() {
        BizCsvInfoDto success = row("111-11-11111");
        BizCsvInfoDto failed = row("222-22-22222");
        BizCsvInfoDto empty = row("333-33-33333");

        CorpMastCreateDTO fromApi = dto("111-11-11111");
        CorpMastCreateDTO failedFallback = dto("222-22-22222");
        CorpMastCreateDTO emptyFallback = dto("333-33-33333");

        when(apiService.processAsync(success, "user"))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(fromApi)));
        CompletableFuture<Optional<CorpMastCreateDTO>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("법인 정보가 응답에 포함되지 않았습니다"));
        when(apiService.processAsync(failed, "user")).thenReturn(failedFuture);
        when(apiService.processAsync(empty, "user"))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(dataMapper.mapToCorpMastCreateDTO(List.of(failed), "user")).thenReturn(List.of(failedFallback));
        when(dataMapper.mapToCorpMastCreateDTO(List.of(empty), "user")).thenReturn(List.of(emptyFallback));

        List<CorpMastCreateDTO> prepared = chunkWriter.prepare(List.of(success, failed, empty), "user", null, null);

        assertThat(prepared).containsExactly(fromApi, failedFallback, emptyFallback);
        verify(dataMapper, never()).mapToCorpMastCreateDTO(List.of(success), "user");
    }

    private BizCsvInfoDto row(String bizNo) {
        return BizCsvInfoDto.builder().bizNo(bizNo).bizNm(bizNo).build();
    }

    private CorpMastCreateDTO dto(String bizNo) {
        return CorpMastCreateDTO.builder().bizNo(bizNo).bizNm(bizNo).build();
    }
}
