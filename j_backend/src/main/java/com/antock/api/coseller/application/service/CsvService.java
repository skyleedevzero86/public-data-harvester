package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.service.strategy.CsvFileReadStrategy;
import com.antock.global.common.constants.CsvConstants;
import com.antock.global.common.exception.CsvParsingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    private static final int HEADER_LINE = 1;
    private static final int MAX_ERROR_LINES = 100;
    private static final int PROGRESS_LOG_INTERVAL = 1000;
    private static final int MINIMUM_REQUIRED_COLUMNS = 6;
    private static final int MINIMUM_BIZ_NO_LENGTH = 10;

    @Value("${csv.file-template}")
    private String fileTemplate;

    private final CsvFileReadStrategy csvFileReadStrategy;

    public List<BizCsvInfoDto> readBizCsv(String city, String district) {
        if (!StringUtils.hasText(city)) {
            throw new CsvParsingException(HttpStatus.BAD_REQUEST, "도시명은 필수입니다.");
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("CSV 파일 읽기");

        log.info("CSV 파일 읽기 시작: city={}, district={}", city, district);

        CsvParsingResult parsingResult = new CsvParsingResult();
        List<BizCsvInfoDto> result = new ArrayList<>();

        try {
            String fileName = mapCityToFileName(city);
            log.info("파일명: {}", fileName);

            result = processCsvFile(fileName, parsingResult);

        } catch (CsvParsingException e) {
            throw e;
        } catch (Exception e) {
            log.error("CSV 파일 처리 중 예상치 못한 예외 발생", e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "CSV 파일 처리 실패: " + e.getMessage());
        } finally {
            stopWatch.stop();
            logProcessingResult(parsingResult, stopWatch.getTotalTimeMillis());
        }

        return result;
    }

    private List<BizCsvInfoDto> processCsvFile(String fileName, CsvParsingResult parsingResult) {
        List<BizCsvInfoDto> result = new ArrayList<>();

        try (BufferedReader reader = csvFileReadStrategy.getBufferedReader(fileName)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                parsingResult.totalLines.incrementAndGet();

                if (shouldSkipLine(lineNumber, line)) {
                    continue;
                }

                processLine(line, lineNumber, result, parsingResult);

                if (shouldStopProcessing(parsingResult.errorLines.get())) {
                    break;
                }

                logProgressIfNeeded(lineNumber, parsingResult);
            }

        } catch (IOException e) {
            log.error("CSV 파일 읽기 중 I/O 오류 발생: {}", fileName, e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "CSV 파일 읽기 실패: " + e.getMessage());
        }

        return result;
    }

    private void processLine(String line, int lineNumber, List<BizCsvInfoDto> result,
                             CsvParsingResult parsingResult) {
        try {
            String[] tokens = parseCsvLine(line);

            if (!isValidData(tokens)) {
                parsingResult.invalidLines.incrementAndGet();
                log.debug("유효하지 않은 데이터 라인 {}: {}", lineNumber, line);
                return;
            }

            if (!isBizType(tokens)) {
                return;
            }

            BizCsvInfoDto dto = parseCsvData(tokens);
            if (dto != null) {
                result.add(dto);
                parsingResult.validLines.incrementAndGet();
            }

        } catch (Exception e) {
            parsingResult.errorLines.incrementAndGet();
            log.warn("라인 {} 파싱 오류: {}", lineNumber, e.getMessage());
        }
    }


    private String[] parseCsvLine(String line) {
        return line.split(",", -1);
    }

    private boolean shouldSkipLine(int lineNumber, String line) {

        if (lineNumber == HEADER_LINE) {
            return true;
        }

        if (!StringUtils.hasText(line)) {
            return true;
        }

        return false;
    }

    private boolean shouldStopProcessing(int errorCount) {
        if (errorCount > MAX_ERROR_LINES) {
            log.error("오류 라인이 {}개를 초과하여 파싱을 중단합니다", MAX_ERROR_LINES);
            return true;
        }
        return false;
    }

    private void logProgressIfNeeded(int lineNumber, CsvParsingResult parsingResult) {
        if (lineNumber % PROGRESS_LOG_INTERVAL == 0) {
            log.info("CSV 파싱 진행 상황: {}줄 처리됨 (유효: {}, 무효: {}, 오류: {})",
                    lineNumber,
                    parsingResult.validLines.get(),
                    parsingResult.invalidLines.get(),
                    parsingResult.errorLines.get());
        }
    }

    private void logProcessingResult(CsvParsingResult parsingResult, long elapsedTimeMs) {
        log.info("CSV 파일 읽기 완료 - 총 라인: {}, 유효: {}, 무효: {}, 오류: {}, 소요시간: {}ms",
                parsingResult.totalLines.get(),
                parsingResult.validLines.get(),
                parsingResult.invalidLines.get(),
                parsingResult.errorLines.get(),
                elapsedTimeMs);
    }

    private String mapCityToFileName(String city) {
        return String.format(fileTemplate, city);
    }

    private boolean isBizType(String[] tokens) {
        if (tokens.length < 4) {
            return false;
        }
        String bizType = tokens[3].trim();
        return CsvConstants.CORP_TYPE_BIZ.equals(bizType);
    }

    private boolean isValidData(String[] tokens) {
        if (tokens.length < MINIMUM_REQUIRED_COLUMNS) {
            return false;
        }

        // 필수 필드 검증
        String sellerId = tokens[0].trim();
        String bizNm = tokens[1].trim();
        String bizNo = tokens[2].trim();
        String bizAddress = tokens[4].trim();

        return StringUtils.hasText(sellerId) &&
                StringUtils.hasText(bizNm) &&
                StringUtils.hasText(bizNo) &&
                StringUtils.hasText(bizAddress) &&
                bizNo.length() >= MINIMUM_BIZ_NO_LENGTH;
    }

    private BizCsvInfoDto parseCsvData(String[] tokens) {
        try {
            return BizCsvInfoDto.builder()
                    .sellerId(tokens[0].trim())
                    .bizNm(tokens[1].trim())
                    .bizNo(tokens[2].trim())
                    .bizType(tokens[3].trim())
                    .bizAddress(tokens[4].trim())
                    .bizNesAddress(tokens.length > 5 ? tokens[5].trim() : "")
                    .build();
        } catch (Exception e) {
            log.warn("CSV 데이터 파싱 실패: {}", String.join(",", tokens), e);
            return null;
        }
    }

    private static class CsvParsingResult {
        private final AtomicInteger totalLines = new AtomicInteger(0);
        private final AtomicInteger validLines = new AtomicInteger(0);
        private final AtomicInteger invalidLines = new AtomicInteger(0);
        private final AtomicInteger errorLines = new AtomicInteger(0);
    }
}