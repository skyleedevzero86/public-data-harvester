package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.global.common.constants.CsvConstants;
import com.antock.global.common.exception.CsvParsingException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CsvService {

    @Value("${csv.file-template}")
    private String fileTemplate;

    @Value("${csv.file-path:classpath}")
    private String filePath;

    @Value("${minio.url:}")
    private String minioUrl;

    @Value("${minio.access-key:}")
    private String minioAccessKey;

    @Value("${minio.secret-key:}")
    private String minioSecretKey;

    @Value("${minio.bucket:}")
    private String minioBucket;

    private MinioClient minioClient;

    public List<BizCsvInfoDto> readBizCsv(String city, String district) {
        String fileName = String.format(fileTemplate, city, district);

        try (BufferedReader br = getBufferedReader(fileName)) {
            List<BizCsvInfoDto> results = br.lines()
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(this::isBiz)
                    .filter(this::isValidData)
                    .map(this::parseCsvData)
                    .collect(Collectors.toList());

            log.info("데이터 필터링 완료: 총 {}개의 유효한 레코드 처리됨", results.size());
            return results;

        } catch (IOException e) {
            log.error("CSV 파일 읽기 실패: {}", e.getMessage(), e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV파일 읽어오기에 실패했습니다.");
        }
    }

    private BufferedReader getBufferedReader(String fileName) throws IOException {
        if ("minio".equals(filePath)) {
            return getMinioBufferedReader(fileName);
        } else if (filePath.startsWith("classpath")) {
            return getClasspathBufferedReader(fileName);
        } else {
            return getFileSystemBufferedReader(fileName);
        }
    }

    /**
     * MinIO에서 CSV 파일 읽기
     */
    private BufferedReader getMinioBufferedReader(String fileName) throws IOException {
        try {
            if (minioClient == null) {
                minioClient = MinioClient.builder()
                        .endpoint(minioUrl)
                        .credentials(minioAccessKey, minioSecretKey)
                        .build();
            }

            String objectName = "csv/" + fileName;

            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(objectName)
                            .build())) {

                // InputStream을 String으로 읽어서 BufferedReader로 변환
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("EUC-KR")))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                return new BufferedReader(new java.io.StringReader(content.toString()));

            } catch (Exception e) {
                log.debug("MinIO에서 요청한 CSV 파일 없음. 기본 파일로 대체. 요청 파일: {}", fileName);
                log.info("MinIO에서 요청한 CSV 파일 없음. 기본 파일로 대체.");
                return getMinioDefaultFile();
            }

        } catch (Exception e) {
            log.error("MinIO 연결 실패: {}", e.getMessage(), e);
            throw new IOException("MinIO에서 파일을 읽는데 실패했습니다.", e);
        }
    }

    /**
     * MinIO에서 기본 파일 읽기
     */
    private BufferedReader getMinioDefaultFile() throws IOException {
        try {
            String defaultFileName = String.format(fileTemplate, CsvConstants.DEFAULT_CITY, CsvConstants.DEFAULT_DISTRICT);
            String defaultObjectName = "csv/" + defaultFileName;

            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(defaultObjectName)
                            .build())) {

                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("EUC-KR")))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                return new BufferedReader(new java.io.StringReader(content.toString()));
            }
        } catch (Exception e) {
            throw new IOException("MinIO에서 기본 파일을 읽는데 실패했습니다.", e);
        }
    }

    /**
     * 클래스패스에서 CSV 파일 읽기 (기존 방식)
     */
    private BufferedReader getClasspathBufferedReader(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("csvFiles/" + fileName);

        try {
            return new BufferedReader(new InputStreamReader(resource.getInputStream(), Charset.forName("EUC-KR")));
        } catch (IOException e) {
            log.debug("클래스패스에서 요청한 CSV 파일 없음. 기본 파일로 대체. 요청 파일: {}", fileName);
            log.info("클래스패스에서 요청한 CSV 파일 없음. 기본 파일로 대체.");
            String defaultFileName = String.format(fileTemplate, CsvConstants.DEFAULT_CITY, CsvConstants.DEFAULT_DISTRICT);
            ClassPathResource defaultResource = new ClassPathResource("csvFiles/" + defaultFileName);
            return new BufferedReader(new InputStreamReader(defaultResource.getInputStream(), Charset.forName("EUC-KR")));
        }
    }

    /**
     * 파일 시스템에서 CSV 파일 읽기
     */
    private BufferedReader getFileSystemBufferedReader(String fileName) throws IOException {
        Path csvPath = Paths.get(filePath, fileName);

        try {
            if (!Files.exists(csvPath)) {
                log.debug("파일 시스템에서 요청한 CSV 파일 없음. 기본 파일로 대체. 요청 파일: {}", fileName);
                log.info("파일 시스템에서 요청한 CSV 파일 없음. 기본 파일로 대체.");
                String defaultFileName = String.format(fileTemplate, CsvConstants.DEFAULT_CITY, CsvConstants.DEFAULT_DISTRICT);
                csvPath = Paths.get(filePath, defaultFileName);
            }

            return Files.newBufferedReader(csvPath, Charset.forName("EUC-KR"));

        } catch (IOException e) {
            log.error("파일 시스템에서 CSV 파일 읽기 실패: {}", e.getMessage(), e);
            throw new IOException("파일 시스템에서 파일을 읽는데 실패했습니다.", e);
        }
    }

    /**
     * "법인여부" 가 "법인" 인지 필터링
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
     */
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
            log.error("데이터 파싱 중 오류 발생: {}", e.getMessage(), e);
            throw new CsvParsingException(HttpStatus.INTERNAL_SERVER_ERROR, "CSV 데이터 파싱 중 오류가 발생했습니다.");
        }
    }
}