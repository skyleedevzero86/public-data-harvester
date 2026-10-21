package com.antock.api.coseller.application.service;

import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ManualRegistrationHistory {

    static final int MESSAGE_LIMIT = 255;

    private static final DateTimeFormatter REGISTERED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CsvBatchHistoryRepository csvBatchHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String username, String city, String district, String fileName, int savedCount, Exception error) {
        boolean success = error == null && savedCount > 0;
        csvBatchHistoryRepository.save(CsvBatchHistory.builder()
                .username(username == null || username.isBlank() ? "system" : username)
                .city(city)
                .district(district)
                .fileName(fileName)
                .recordCount(savedCount)
                .status(success ? "SUCCESS" : "FAIL")
                .message(success ? "성공(" + savedCount + "건)" : failureText(savedCount, error))
                .timestamp(LocalDateTime.now())
                .build());
    }

    public List<Row> recent() {
        return csvBatchHistoryRepository.findTop20ByUsernameIsNotNullOrderByTimestampDesc().stream()
                .map(this::toRow)
                .toList();
    }

    private Row toRow(CsvBatchHistory history) {
        LocalDateTime timestamp = history.getTimestamp();
        return new Row(
                history.getUsername(),
                history.getCity(),
                history.getDistrict(),
                timestamp == null ? "" : timestamp.format(REGISTERED_AT),
                history.getMessage(),
                "SUCCESS".equals(history.getStatus()));
    }

    private String failureText(int savedCount, Exception error) {
        String reason = error == null || error.getMessage() == null || error.getMessage().isBlank()
                ? savedCount + "건"
                : error.getMessage().replace('\n', ' ').replace('\r', ' ');
        String text = "실패(" + reason + ")";
        if (text.length() <= MESSAGE_LIMIT) {
            return text;
        }
        return text.substring(0, MESSAGE_LIMIT);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Row {
        private final String username;
        private final String city;
        private final String district;
        private final String registeredAt;
        private final String result;
        private final boolean success;
    }
}
