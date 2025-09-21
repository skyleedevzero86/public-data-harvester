package com.antock.api.coseller.application.service.strategy;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "csv.file-path", havingValue = "minio")
public class MinioFileReadStrategy implements CsvFileReadStrategy {

    private final MinioClient minioClient;

    @Value("${minio.bucket:mybucket}")
    private String bucketName;

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_");

    @Override
    public BufferedReader getBufferedReader(String fileName) throws IOException {
        try {
            if (!isMinioAvailable()) {
                throw new IOException("MinIO 서버에 연결할 수 없습니다. 서버 상태를 확인해주세요.");
            }

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
            throw new IOException("MinIO에서 파일을 읽을 수 없습니다: " + fileName, e);
        } catch (Exception e) {
            throw new IOException("MinIO 파일 로드 실패: " + fileName, e);
        }
    }

    @Override
    public InputStream readFile(String fileName) {
        try {
            if (!isMinioAvailable()) {
                throw new RuntimeException("MinIO 서버에 연결할 수 없습니다. 서버 상태를 확인해주세요.");
            }

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
            throw new RuntimeException("MinIO에서 파일을 읽을 수 없습니다: " + fileName, e);
        } catch (Exception e) {
            throw new RuntimeException("MinIO 파일 로드 실패: " + fileName, e);
        }
    }

    private String findActualFileName(String targetFileName) {
        try {
            Iterable<io.minio.Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build());

            int fileCount = 0;
            for (io.minio.Result<Item> result : objects) {
                Item item = result.get();
                String objectName = item.objectName();
                fileCount++;

                String fileNameWithoutUuid = removeUuidPrefix(objectName);

                if (targetFileName.equals(fileNameWithoutUuid) ||
                        fileNameWithoutUuid.endsWith(targetFileName) ||
                        objectName.endsWith(targetFileName)) {
                    return objectName;
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    private String removeUuidPrefix(String fileName) {
        if (fileName == null) {
            return null;
        }

        if (UUID_PATTERN.matcher(fileName).find()) {
            String fileNameWithoutUuid = fileName.replaceFirst(UUID_PATTERN.pattern(), "");
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
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            return false;
        } catch (Exception e) {
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
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }

    public String getBucketName() {
        return bucketName;
    }
}