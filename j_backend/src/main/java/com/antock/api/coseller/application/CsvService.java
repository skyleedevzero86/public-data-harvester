package com.antock.api.coseller.application;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.global.common.constants.CsvConstants;
import com.antock.global.common.exception.CsvParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsvService {

    @Value("${csv.file-template}")
    private String fileTemplate; // application.yml에서 설정한 파일 경로 템플릿

    public List<BizCsvInfoDto> readBizCsv(String city, String district) {
        String fileName = String.format(fileTemplate, city, district); // city_district.csv
        ClassPathResource resource = new ClassPathResource("csvFiles/" + fileName);

        try (BufferedReader br = getReaderFromDefault(resource)) {
            List<BizCsvInfoDto> results = br.lines()
                    .skip(1)
                    .map(line -> line.split(",", -1)) // csv라인을 읽어서 String으로 넘김
                    .filter(this::isBiz)  // 법인여부 필터링
                    .filter(this::isValidData) // Null, N/A, 0000000000000 필터링
                    .map(this::parseCsvData) // String[]를 BizCsvInfoDto로 변환
                    .collect(Collectors.toList());

            log.info("데이터 필터링 완료: 총 {}개의 유효한 레코드 처리됨", results.size());
            return results;

        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패: {}", e.getMessage(), e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV파일 읽어오기에 실패했습니다.");
        }
    }

    /**
     * 요청받은 city, district 로 데이터가 없는경우 "서울특별시_강남구 csv동작"
     * @param resource
     * @return
     * @throws IOException
     */
    private BufferedReader getReaderFromDefault(ClassPathResource resource) throws IOException {
        try {
            return new BufferedReader(new InputStreamReader(resource.getInputStream(), Charset.forName("EUC-KR")));
        } catch (IOException e) {
            log.debug("요청한 CSV 파일 없음. 기본 파일로 대체. 요청 파일: {}", resource.getFilename());
            log.info("요청한 CSV 파일 없음. 기본 파일로 대체.");
            String defaultFileName = String.format(fileTemplate, CsvConstants.DEFAULT_CITY, CsvConstants.DEFAULT_DISTRICT);
            ClassPathResource defaultResource = new ClassPathResource("csvFiles/" + defaultFileName);
            return new BufferedReader(new InputStreamReader(defaultResource.getInputStream(), Charset.forName("EUC-KR")));
        }
    }

    /**
     * "법인여부" 가 "법인" 인지 필터링
     * @param tokens
     * @return
     */
    private boolean isBiz(String[] tokens) {
        boolean result = tokens.length > 4 && CsvConstants.CORP_TYPE_BIZ.equals(tokens[4].trim());
        if (!result && tokens.length > 4) {
            log.debug("법인 아님 필터링: bizType={}", tokens[4].trim());
        }
        return result;
    }

    /**
     * 데이터 유효성 검사: Null, '%N/A%' 및 0000000000000 제외
     * @param tokens
     * @return
     */
    private boolean isValidData(String[] tokens) {
        if (tokens.length <= 3) {
            log.debug("유효하지 않은 데이터: 토큰 길이 부족 (length={})", tokens.length);
            return false;
        }

        String corpRegNo = tokens[3].trim();

        // 빈 값 체크
        if (corpRegNo == null || corpRegNo.isEmpty()) {
            log.debug("유효하지 않은 데이터: 빈 값");
            return false;
        }

        // '0000000000000' 체크 - 정확히 13자리 0인지
        if ("0000000000000".equals(corpRegNo)) {
            log.debug("유효하지 않은 데이터: '0000000000000' 값 (corpRegNo={})", corpRegNo);
            return false;
        }

        // 'N/A' 문자열 체크 - 정확히 일치하는지 (대소문자 무시)
        if (corpRegNo.toUpperCase().contains("N/A")) {
            log.debug("유효하지 않은 데이터: 'N/A' 포함 (corpRegNo={})", corpRegNo);
            return false;
        }

        // 추가 검증 - 모든 문자가 0인지 확인
        if (corpRegNo.matches("^0+$")) {
            log.debug("유효하지 않은 데이터: 모든 문자가 0 (corpRegNo={})", corpRegNo);
            return false;
        }

        return true;
    }

    /**
     * CSV 데이터를 BizCsvInfoDto로 파싱
     * @param tokens
     * @return
     */
    private BizCsvInfoDto parseCsvData(String[] tokens) {
        try {
            // 배열 길이 체크하여 안전하게 파싱
            return BizCsvInfoDto.builder()
                    .sellerId(tokens.length > 0 ? tokens[0].trim() : "")
                    .bizNm(tokens.length > 2 ? tokens[2].trim() : "")
                    .bizNo(tokens.length > 3 ? tokens[3].trim() : "")
                    .bizType(tokens.length > 4 ? tokens[4].trim() : "")
                    .bizAddress(tokens.length > 9 ? tokens[9].trim() : "")
                    .bizNesAddress(tokens.length > 10 ? tokens[10].trim() : "")
                    .build();
        } catch (Exception e) {
            log.error("데이터 파싱 중 오류 발생: {}", e.getMessage(), e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV 데이터 파싱 중 오류가 발생했습니다.");
        }
    }
}