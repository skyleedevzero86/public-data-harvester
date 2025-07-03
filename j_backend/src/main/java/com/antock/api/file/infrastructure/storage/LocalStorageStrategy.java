package com.antock.api.file.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class LocalStorageStrategy implements FileStorageStrategy {

    @Value("${file.upload-dir:/tmp/uploads}")
    private String uploadDir;

    @Override
    public String uploadFile(MultipartFile file, String storedFileName) throws Exception {
        createDirectoryIfNotExists();

        Path filePath = Paths.get(uploadDir, storedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("파일 로컬 저장 완료: {}", filePath);
        return storedFileName;
    }

    @Override
    public InputStreamResource downloadFile(String storedFileName) throws Exception {
        Path filePath = Paths.get(uploadDir, storedFileName);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("파일을 찾을 수 없습니다: " + storedFileName);
        }
        return new InputStreamResource(new FileInputStream(filePath.toFile()));
    }

    @Override
    public String getDownloadUrl(String storedFileName) throws Exception {
        return null;
    }

    @Override
    public void deleteFile(String storedFileName) throws Exception {
        Path filePath = Paths.get(uploadDir, storedFileName);
        Files.deleteIfExists(filePath);
        log.info("파일 삭제 완료: {}", filePath);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private void createDirectoryIfNotExists() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("업로드 디렉토리 생성: {}", uploadPath);
        }
    }
}