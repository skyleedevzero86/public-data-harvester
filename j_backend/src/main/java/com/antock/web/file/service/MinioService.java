package com.antock.web.file.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    /**
     * 버킷이 존재하지 않으면 생성합니다.
     */
    public void createBucket() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    /**
     * MinIO에 파일을 업로드합니다.
     * @param file MultipartFile
     * @param objectName MinIO에 저장될 객체 이름
     * @return 성공 여부
     */
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        createBucket(); // 버킷이 존재하는지 확인하고 없으면 생성

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return objectName;
        }
    }

    /**
     * MinIO에서 파일을 다운로드합니다.
     * @param objectName MinIO에 저장된 객체 이름
     * @return 파일 InputStreamResource
     */
    public InputStreamResource downloadFile(String objectName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        return new InputStreamResource(stream);
    }

    /**
     * MinIO에서 파일의 사전 서명된 URL을 가져옵니다. (다운로드 링크)
     * @param objectName MinIO에 저장된 객체 이름
     * @return 사전 서명된 URL
     */
    public String getPreSignedUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(7, TimeUnit.DAYS) // 7일 유효
                        .build());
    }

    /**
     * MinIO에서 파일을 삭제합니다.
     * @param objectName MinIO에 저장된 객체 이름
     */
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }
}