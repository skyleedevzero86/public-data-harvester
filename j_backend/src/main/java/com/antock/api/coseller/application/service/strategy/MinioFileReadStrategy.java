package com.antock.api.coseller.application.service.strategy;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@Slf4j
@Component
@Profile("dev")
public class MinioFileReadStrategy implements CsvFileReadStrategy {

    @Value("${minio.bucket}")
    private String minioBucket;

    private final MinioClient minioClient;

    public MinioFileReadStrategy(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public BufferedReader getBufferedReader(String fileName) throws IOException {
        try {
            String objectName =  fileName;
            log.info("MinIO에서 파일 요청: bucket={}, object={}", minioBucket, objectName);

            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucket)
                            .object(objectName)
                            .build());

            log.info("MinIO에서 파일 '{}' 성공적으로 로드됨", objectName);
            return new BufferedReader(new InputStreamReader(stream, Charset.forName("EUC-KR")));
        } catch (Exception e) {
            log.error("MinIO에서 파일 읽기 실패: {}", e.getMessage(), e);
            throw new IOException("MinIO에서 파일을 읽을 수 없습니다: " + fileName, e);
        }
    }
}