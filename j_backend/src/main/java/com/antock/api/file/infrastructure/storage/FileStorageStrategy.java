package com.antock.api.file.infrastructure.storage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageStrategy {
    String uploadFile(MultipartFile file, String storedFileName) throws Exception;
    InputStreamResource downloadFile(String storedFileName) throws Exception;
    String getDownloadUrl(String storedFileName) throws Exception;
    void deleteFile(String storedFileName) throws Exception;
    boolean isAvailable();
}