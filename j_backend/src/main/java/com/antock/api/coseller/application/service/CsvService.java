package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.service.strategy.CsvFileReadStrategy;
import com.antock.global.common.constants.CsvConstants;
import com.antock.global.common.exception.CsvParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsvService {

    @Value("${csv.file-template}")
    private String fileTemplate;

    private final CsvFileReadStrategy csvFileReadStrategy;

    public CsvService(CsvFileReadStrategy csvFileReadStrategy) {
        this.csvFileReadStrategy = csvFileReadStrategy;
    }

    public List<BizCsvInfoDto> readBizCsv(String city, String district) {
        String fileName = String.format(fileTemplate, mapCityToFileName(city), district);

        try (BufferedReader br = csvFileReadStrategy.getBufferedReader(fileName)) {
            List<BizCsvInfoDto> results = br.lines()
                    .peek(line -> log.trace("Processing line: {}", line))
                    .skip(1) // 헤더 스킵
                    .map(line -> line.split(",", -1))
                    .peek(tokens -> {
                        if (!isBiz(tokens)) {
                            log.debug("필터링됨 (법인 아님): bizType={}, line={}",
                                    tokens.length > 4 ? tokens[4].trim() : "N/A",
                                    String.join(",", tokens));
                        }
                    })
                    .filter(this::isBiz)
                    .peek(tokens -> {
                        if (!isValidData(tokens)) {
                            log.debug("필터링됨 (유효하지 않은 데이터): bizNo={}, line={}",
                                    tokens.length > 3 ? tokens[3].trim() : "N/A",
                                    String.join(",", tokens));
                        }
                    })
                    .filter(this::isValidData)
                    .map(tokens -> {
                        try {
                            return parseCsvData(tokens);
                        } catch (CsvParsingException e) {
                            log.error("CSV 데이터 파싱 중 오류 발생: line={}. 스킵합니다.",
                                    String.join(",", tokens), e);
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("CSV 파일 읽기 및 필터링 완료: 총 {}개의 유효한 레코드 처리됨", results.size());
            return results;
        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패: 파일명={}. 오류: {}", fileName, e.getMessage(), e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV 파일 읽어오기에 실패했습니다.");
        }
    }

    private String mapCityToFileName(String city) {
        return city;
    }

    private boolean isBiz(String[] tokens) {
        boolean result = tokens.length > 4 && CsvConstants.CORP_TYPE_BIZ.equals(tokens[4].trim());
        if (!result && tokens.length > 4) {
            log.debug("법인 아님 필터링됨: bizType={}", tokens[4].trim());
        } else if (!result) {
            log.debug("법인 아님 필터링됨: 토큰 길이 부족 (length={})", tokens.length);
        }
        return result;
    }

    private boolean isValidData(String[] tokens) {
        if (tokens.length <= 3) {
            log.debug("유효하지 않은 데이터: 토큰 길이 부족 (length={})", tokens.length);
            return false;
        }

        String corpRegNo = tokens[3].trim();

        if (corpRegNo.isEmpty()) {
            log.debug("유효하지 않은 데이터: 법인 등록 번호 빈 값");
            return false;
        }

        if ("0000000000000".equals(corpRegNo) ||
                corpRegNo.toUpperCase().contains("N/A") ||
                corpRegNo.matches("^0+$")) {
            log.debug("유효하지 않은 데이터: 필터링 값 (corpRegNo={})", corpRegNo);
            return false;
        }
        return true;
    }

    private BizCsvInfoDto parseCsvData(String[] tokens) {
        try {
            return BizCsvInfoDto.builder()
                    .sellerId(tokens.length > 0 ? tokens[0].trim() : "")
                    .bizNm(tokens.length > 2 ? tokens[2].trim() : "")
                    .bizNo(tokens.length > 3 ? tokens[3].trim() : "")
                    .bizType(tokens.length > 4 ? tokens[4].trim() : "")
                    .bizAddress(tokens.length > 9 ? tokens[9].trim() : "")
                    .bizNesAddress(tokens.length > 10 ? tokens[10].trim() : "")
                    .build();
        } catch (Exception e) {
            log.error("CSV 데이터 파싱 중 오류 발생: 토큰={}. 오류: {}",
                    String.join(",", tokens), e.getMessage(), e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "CSV 데이터 파싱 중 오류가 발생했습니다.");
        }
    }
}