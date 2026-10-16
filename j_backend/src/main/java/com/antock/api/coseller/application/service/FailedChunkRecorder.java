package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedChunkRecorder {

    private static final int MESSAGE_LIMIT = 250;

    private final CsvBatchHistoryRepository csvBatchHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String fileName, int startIndex, List<BizCsvInfoDto> chunk, Exception error) {
        int size = chunk == null ? 0 : chunk.size();
        log.warn("청크 저장 실패: file={}, startIndex={}, size={}", fileName, startIndex, size, error);
        csvBatchHistoryRepository.save(CsvBatchHistory.builder()
                .fileName(fileName)
                .recordCount(size)
                .status("FAIL")
                .message(limit(failureMessage(startIndex, error)))
                .timestamp(LocalDateTime.now())
                .build());
    }

    private String failureMessage(int startIndex, Exception error) {
        String type = error == null ? "unknown" : error.getClass().getSimpleName();
        return "startIndex=" + startIndex + ", " + type;
    }

    private String limit(String message) {
        if (message.length() <= MESSAGE_LIMIT) {
            return message;
        }
        return message.substring(0, MESSAGE_LIMIT);
    }
}
