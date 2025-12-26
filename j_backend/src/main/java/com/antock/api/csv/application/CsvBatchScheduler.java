package com.antock.api.csv.application;

import com.antock.api.csv.application.service.CsvBatchService;
import com.antock.api.csv.domain.CsvBatchHistory;
import com.antock.api.csv.infrastructure.CsvBatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvBatchScheduler {

    private final CsvBatchService batchService;
    private final CsvBatchHistoryRepository historyRepository;

    @Scheduled(cron = "0 0 6 ? * MON")
    public void runCsvBatch() {
        log.info("CSV 배치 스케줄러 실행 시작");

        try {
            List<CsvBatchHistory> recentFailures = historyRepository.findTop10ByOrderByTimestampDesc();
            boolean hasRecentFailures = recentFailures.stream()
                    .anyMatch(h -> "ERROR".equals(h.getStatus()) &&
                            h.getTimestamp().isAfter(LocalDateTime.now().minusDays(1)));

            if (hasRecentFailures) {
                log.warn("최근 24시간 내 배치 실패 이력이 있어 수동 확인이 필요합니다");
            }
            batchService.runBatch();
            log.info("CSV 배치 스케줄러 실행 완료");

        } catch (Exception e) {
            log.error("CSV 배치 스케줄러 실행 중 오류 발생", e);
            saveFailureHistory(e);
            notifyAdministrator(e);
        }
    }

    private void saveFailureHistory(Exception e) {
        try {
            CsvBatchHistory failureHistory = CsvBatchHistory.builder()
                    .city("SCHEDULER")
                    .district("ERROR")
                    .fileName("scheduler_failure.log")
                    .recordCount(0)
                    .status("SCHEDULER_ERROR")
                    .message("스케줄러 실행 실패: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();

            historyRepository.save(failureHistory);

        } catch (Exception saveException) {
            log.error("실패 이력 저장 중 오류 발생", saveException);
        }
    }

    private void notifyAdministrator(Exception e) {
        log.error("관리자 알림 필요: CSV 배치 스케줄러 실패 - {}", e.getMessage());
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanupOldHistory() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            log.debug("30일 이전 배치 이력 정리 시작");

        } catch (Exception e) {
            log.error("배치 이력 정리 중 오류 발생", e);
        }
    }
}



