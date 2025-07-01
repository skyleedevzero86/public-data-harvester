package com.antock.api.coseller.application.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@Profile("prod")
public class LocalFileReadStrategy implements CsvFileReadStrategy {

    @Value("${csv.file-path}")
    private String csvFilePath;

    @Override
    public BufferedReader getBufferedReader(String fileName) throws IOException {
        try {
            Path filePath = Paths.get(csvFilePath, fileName);
            log.info("로컬 파일에서 읽기 시도: {}", filePath.toString());

            if (!Files.exists(filePath)) {
                log.error("파일이 존재하지 않습니다: {}", filePath.toString());
                throw new IOException("파일을 찾을 수 없습니다: " + filePath.toString());
            }

            log.info("로컬 파일 '{}' 성공적으로 로드됨", filePath.toString());
            return Files.newBufferedReader(filePath, Charset.forName("EUC-KR"));
        } catch (Exception e) {
            log.error("로컬 파일 읽기 실패: {}", e.getMessage(), e);
            throw new IOException("로컬 파일을 읽을 수 없습니다: " + fileName, e);
        }
    }
}
