package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.service.strategy.CsvFileReadStrategy;
import com.antock.global.common.constants.CsvConstants;
import com.antock.global.common.exception.CsvParsingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    private final CsvFileReadStrategy csvFileReadStrategy;

    @Value("${app.csv.encoding:UTF-8}")
    private String csvEncoding;

    @Value("${app.csv.buffer-size:8192}")
    private int bufferSize;

    @Value("${app.csv.max-lines:10000}")
    private int maxLines;

    public CsvFileReadStrategy getFileReadStrategy() {
        return csvFileReadStrategy;
    }

    public List<BizCsvInfoDto> readCsvFile(String fileName) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("CSV 파일 읽기");

        List<BizCsvInfoDto> validData = new ArrayList<>();
        List<String> invalidLines = new ArrayList<>();
        int errorCount = 0;
        int totalLines = 0;

        try (InputStream inputStream = csvFileReadStrategy.readFile(fileName);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, detectEncoding(inputStream)), bufferSize)) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null && totalLines < maxLines) {
                totalLines++;

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] tokens = parseCsvLine(line);

                    if (tokens != null && tokens.length >= CsvConstants.MIN_REQUIRED_COLUMNS) {
                        BizCsvInfoDto dto = parseCsvData(tokens);
                        if (dto != null) {
                            validData.add(dto);
                        } else {
                            invalidLines.add(line);
                        }
                    } else {
                        invalidLines.add(line);
                    }
                } catch (Exception e) {
                    errorCount++;
                }
            }

        } catch (IOException e) {
            throw new CsvParsingException("CSV 파일을 읽을 수 없습니다: " + fileName, e);
        }

        stopWatch.stop();
        long processingTime = stopWatch.getTotalTimeMillis();

        return validData;
    }

    private Charset detectEncoding(InputStream inputStream) {
        try {
            inputStream.mark(3);
            byte[] bom = new byte[3];
            int bytesRead = inputStream.read(bom);
            inputStream.reset();

            if (bytesRead >= 3 &&
                    bom[0] == (byte) 0xEF &&
                    bom[1] == (byte) 0xBB &&
                    bom[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8;
            }

            inputStream.mark(1024);
            byte[] buffer = new byte[1024];
            int bytesRead2 = inputStream.read(buffer);
            inputStream.reset();

            if (bytesRead2 > 0) {
                String testString = new String(buffer, 0, bytesRead2, StandardCharsets.UTF_8);
                if (isValidKoreanText(testString)) {
                    return StandardCharsets.UTF_8;
                }

                testString = new String(buffer, 0, bytesRead2, Charset.forName("EUC-KR"));
                if (isValidKoreanText(testString)) {
                    return Charset.forName("EUC-KR");
                }

                testString = new String(buffer, 0, bytesRead2, Charset.forName("CP949"));
                if (isValidKoreanText(testString)) {
                    return Charset.forName("CP949");
                }
            }

            return StandardCharsets.UTF_8;

        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private boolean isValidKoreanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        return text.matches(".*[가-힣a-zA-Z0-9\\s\\-_.,()]+.*");
    }

    private String[] parseCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (escapeNext) {
                currentToken.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(currentToken.toString().trim());
                currentToken = new StringBuilder();
            } else {
                currentToken.append(c);
            }
        }

        tokens.add(currentToken.toString().trim());

        return tokens.toArray(new String[0]);
    }

    private BizCsvInfoDto parseCsvData(String[] tokens) {
        try {
            return BizCsvInfoDto.builder()
                    .sellerId(tokens[0].trim())
                    .city(tokens[1].trim())
                    .bizNm(tokens[2].trim())
                    .bizNo(tokens[3].trim())
                    .bizType(tokens[4].trim())
                    .ownerName(tokens[5].trim())
                    .phone(tokens[6].trim())
                    .email(tokens[7].trim())
                    .date(tokens[8].trim())
                    .address(tokens[9].trim())
                    .bizAddress(tokens[9].trim())
                    .bizNesAddress(tokens.length > 10 ? tokens[10].trim() : "")
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}