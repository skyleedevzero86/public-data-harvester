package com.antock.api.coseller.application.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    @Value("${file.upload.path:./data/csv/}")
    private String uploadPath;

    @Override
    public BufferedReader getBufferedReader(String fileName) throws IOException {
        Path filePath = resolveSafely(fileName);
        log.info("로컬 파일 읽기: {}", fileName);

        if (!Files.exists(filePath)) {
            throw new IOException("파일을 찾을 수 없습니다: " + fileName);
        }

        return new BufferedReader(new FileReader(filePath.toFile()));
    }

    @Override
    public InputStream readFile(String fileName) {
        try {
            Path filePath = resolveSafely(fileName);
            log.info("로컬 파일 InputStream 읽기: {}", fileName);

            if (!Files.exists(filePath)) {
                throw new IOException("파일을 찾을 수 없습니다: " + fileName);
            }

            return new FileInputStream(filePath.toFile());

        } catch (IOException e) {
            log.error("로컬 파일 '{}' 읽기 실패", fileName, e);
            throw new RuntimeException("로컬 파일 읽기 실패: " + fileName, e);
        }
    }

    Path resolveSafely(String fileName) {
        Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path target = base.resolve(fileName).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("허용되지 않은 파일 경로입니다.");
        }
        return target;
    }
}