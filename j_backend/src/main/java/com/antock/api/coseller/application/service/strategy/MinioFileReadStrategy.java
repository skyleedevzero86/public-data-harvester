package com.antock.api.coseller.application.service.strategy;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioFileReadStrategy implements CsvFileReadStrategy {

    private final MinioClient minioClient;

    @Value("${minio.bucket:mybucket}")
    private String bucketName;

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_");

    @Override
    public BufferedReader getBufferedReader(String fileName) throws IOException {
        try {
            log.info("MinIO에서 파일 읽기 시작: {} (버킷: {})", fileName, bucketName);

            String actualFileName = findActualFileName(fileName);
            if (actualFileName == null) {
                throw new IOException("파일이 MinIO 버킷에 존재하지 않습니다: " + fileName);
            }

            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(actualFileName)
                            .build());

            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (ErrorResponseException e) {
            log.error("MinIO 파일 읽기 실패 (ErrorResponse): {} - {}", fileName, e.getMessage());
            throw new IOException("MinIO에서 파일을 읽을 수 없습니다: " + fileName, e);
        } catch (Exception e) {
            log.error("MinIO 파일 읽기 실패: {} - {}", fileName, e.getMessage(), e);
            throw new IOException("MinIO 파일 로드 실패: " + fileName, e);
        }
    }

    @Override
    public InputStream readFile(String fileName) {
        try {
            log.info("MinIO에서 파일 스트림 읽기 시작: {} (버킷: {})", fileName, bucketName);

            String actualFileName = findActualFileName(fileName);
            if (actualFileName == null) {
                throw new RuntimeException("파일이 MinIO 버킷에 존재하지 않습니다: " + fileName);
            }

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(actualFileName)
                            .build());
        } catch (ErrorResponseException e) {
            log.error("MinIO 파일 스트림 읽기 실패 (ErrorResponse): {} - {}", fileName, e.getMessage());
            throw new RuntimeException("MinIO에서 파일을 읽을 수 없습니다: " + fileName, e);
        } catch (Exception e) {
            log.error("MinIO 파일 스트림 읽기 실패: {} - {}", fileName, e.getMessage(), e);
            throw new RuntimeException("MinIO 파일 로드 실패: " + fileName, e);
        }
    }

    private String findActualFileName(String targetFileName) {
        try {
            log.debug("MinIO에서 파일명 매칭 시도: {} (버킷: {})", targetFileName, bucketName);

            Iterable<io.minio.Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build());

            for (io.minio.Result<Item> result : objects) {
                Item item = result.get();
                String objectName = item.objectName();

                String fileNameWithoutUuid = removeUuidPrefix(objectName);

                if (targetFileName.equals(fileNameWithoutUuid)) {
                    log.info("파일명 매칭 성공: {} -> {}", targetFileName, objectName);
                    return objectName;
                }
            }

            log.warn("MinIO에서 매칭되는 파일을 찾을 수 없음: {} (버킷: {})", targetFileName, bucketName);
            return null;

        } catch (Exception e) {
            log.error("MinIO 파일명 매칭 중 오류: {} - {}", targetFileName, e.getMessage());
            return null;
        }
    }

    private String removeUuidPrefix(String fileName) {
        if (fileName == null) {
            return null;
        }

        if (UUID_PATTERN.matcher(fileName).find()) {
            String fileNameWithoutUuid = fileName.replaceFirst(UUID_PATTERN.pattern(), "");
            log.debug("UUID prefix 제거: {} -> {}", fileName, fileNameWithoutUuid);
            return fileNameWithoutUuid;
        }

        return fileName;
    }

    private boolean fileExists(String fileName) {
        try {

            String actualFileName = findActualFileName(fileName);
            if (actualFileName == null) {
                return false;
            }

            minioClient.statObject(
                    io.minio.StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(actualFileName)
                            .build());
            log.info("MinIO 파일 존재 확인: {} -> {} (버킷: {})", fileName, actualFileName, bucketName);
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.warn("MinIO 파일이 존재하지 않음: {} (버킷: {})", fileName, bucketName);
                return false;
            }
            log.error("MinIO 파일 존재 확인 실패: {} - {}", fileName, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("MinIO 파일 존재 확인 중 오류: {} - {}", fileName, e.getMessage());
            return false;
        }
    }

    public boolean isMinioAvailable() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());

            if (!bucketExists) {
                log.warn("MinIO 버킷이 존재하지 않음: {}", bucketName);
                return false;
            }

            log.info("MinIO 연결 및 버킷 상태 정상: {}", bucketName);
            return true;
        } catch (Exception e) {
            log.error("MinIO 연결 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}