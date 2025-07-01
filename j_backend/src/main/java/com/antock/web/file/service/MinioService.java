package com.antock.web.file.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {

    private final Optional<MinioClient> minioClient;

    @Value("${minio.bucket:default-bucket}")
    private String bucketName;

    // Optional로 MinioClient 주입 (없어도 됨)
    public MinioService(@Autowired(required = false) MinioClient minioClient) {
        this.minioClient = Optional.ofNullable(minioClient);
    }

    private void validateMinioAvailable() {
        if (minioClient.isEmpty()) {
            throw new UnsupportedOperationException("MinIO is not available in this environment");
        }
    }

    public void createBucket() throws Exception {
        validateMinioAvailable();
        boolean found = minioClient.get().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.get().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        validateMinioAvailable();
        createBucket();

        try (InputStream is = file.getInputStream()) {
            minioClient.get().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return objectName;
        }
    }

    public InputStreamResource downloadFile(String objectName) throws Exception {
        validateMinioAvailable();
        InputStream stream = minioClient.get().getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        return new InputStreamResource(stream);
    }

    public String getPreSignedUrl(String objectName) throws Exception {
        validateMinioAvailable();
        return minioClient.get().getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(7, TimeUnit.DAYS)
                        .build());
    }

    public void deleteFile(String objectName) throws Exception {
        validateMinioAvailable();
        minioClient.get().removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }

    public boolean isMinioAvailable() {
        return minioClient.isPresent();
    }
}