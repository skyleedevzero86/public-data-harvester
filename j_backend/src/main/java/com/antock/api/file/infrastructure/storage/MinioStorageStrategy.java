package com.antock.api.file.infrastructure.storage;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageStrategy implements FileStorageStrategy {

    private final Optional<MinioClient> minioClient;

    @Value("${minio.bucket:default-bucket}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String storedFileName) throws Exception {
        validateMinioAvailable();
        createBucketIfNotExists();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.get().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return storedFileName;
        }
    }

    @Override
    public InputStreamResource downloadFile(String storedFileName) throws Exception {
        validateMinioAvailable();
        InputStream stream = minioClient.get().getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storedFileName)
                        .build()
        );
        return new InputStreamResource(stream);
    }

    @Override
    public String getDownloadUrl(String storedFileName) throws Exception {
        validateMinioAvailable();
        return minioClient.get().getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(storedFileName)
                        .expiry(7, TimeUnit.DAYS)
                        .build()
        );
    }

    @Override
    public void deleteFile(String storedFileName) throws Exception {
        validateMinioAvailable();
        minioClient.get().removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storedFileName)
                        .build()
        );
    }

    @Override
    public boolean isAvailable() {
        return minioClient.isPresent();
    }

    private void validateMinioAvailable() {
        if (!isAvailable()) {
            throw new UnsupportedOperationException("MinIO가 사용 불가능합니다.");
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean found = minioClient.get().bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!found) {
            minioClient.get().makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("MinIO 버킷 생성 완료: {}", bucketName);
        }
    }
}