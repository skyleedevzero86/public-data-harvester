package com.antock.api.csv.application;

import com.antock.api.csv.application.service.CsvBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvBatchScheduler {
    private final CsvBatchService batchService;

    @Scheduled(cron = "0 0 6 ? * MON")
    //@Scheduled(cron = "0 * * * * ?")
    public void runCsvBatch() {
        log.info("CSV 배치 작업 시작");
        batchService.runBatch();
        log.info("CSV 배치 작업 종료");
    }
}