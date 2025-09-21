package com.antock.api.coseller.application.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@ConditionalOnProperty(name = "csv.file-path", havingValue = "local")
public class LocalFileReadStrategy implements CsvFileReadStrategy {

    @Value("${file.upload.path:classpath:CSVFile/}")
    private String uploadPath;

    @Override
    public BufferedReader getBufferedReader(String fileName) throws IOException {
        Path filePath = Paths.get(uploadPath, fileName);
        log.info("로컬 파일 읽기: {}", filePath);

        if (!Files.exists(filePath)) {
            throw new IOException("파일을 찾을 수 없습니다: " + filePath);
        }

        return new BufferedReader(new FileReader(filePath.toFile()));
    }

    @Override
    public InputStream readFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadPath, fileName);
            log.info("로컬 파일 InputStream 읽기: {}", filePath);

            if (!Files.exists(filePath)) {
                throw new IOException("파일을 찾을 수 없습니다: " + filePath);
            }

            return new FileInputStream(filePath.toFile());

        } catch (IOException e) {
            log.error("로컬 파일 '{}' 읽기 실패: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("로컬 파일 읽기 실패: " + fileName, e);
        }
    }
}