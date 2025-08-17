package com.antock.api.file.application.service;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileValidationService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "txt", "rtf",
            "xls", "xlsx", "csv",
            "ppt", "pptx",
            "zip", "rar", "7z", "tar", "gz",
            "mp3", "wav", "ogg",
            "mp4", "avi", "mov", "mkv"
    );

    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar"
    );

    private static final Pattern INVALID_FILENAME_PATTERN = Pattern.compile("[<>:\"/\\|?*]");

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    @Value("${file.upload.max-filename-length:255}")
    private int maxFilenameLength;

    public void validateUploadFile(MultipartFile file) {
        validateFileExists(file);
        validateFileName(file.getOriginalFilename());
        validateFileSize(file.getSize());
        validateFileExtension(file.getOriginalFilename());
        validateContentType(file.getContentType());
    }

    private void validateFileExists(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("업로드할 파일이 없습니다.");
            throw new BusinessException(ErrorCode.FILE_EMPTY, "업로드할 파일이 없습니다.");
        }
    }

    private void validateFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            log.warn("파일명을 확인할 수 없습니다.");
            throw new BusinessException(ErrorCode.FILE_NAME_INVALID, "파일명을 확인할 수 없습니다.");
        }

        if (originalFileName.length() > maxFilenameLength) {
            log.warn("파일명이 너무 깁니다: {} (최대: {})", originalFileName.length(), maxFilenameLength);
            throw new BusinessException(ErrorCode.FILE_NAME_INVALID,
                    "파일명이 너무 깁니다. (최대 " + maxFilenameLength + "자)");
        }

        if (INVALID_FILENAME_PATTERN.matcher(originalFileName).find()) {
            log.warn("파일명에 허용되지 않는 문자가 포함되어 있습니다: {}", originalFileName);
            throw new BusinessException(ErrorCode.FILE_NAME_INVALID,
                    "파일명에 허용되지 않는 문자가 포함되어 있습니다: < > : \" / \\ | ? *");
        }

        String fileNameWithoutExtension = getFileNameWithoutExtension(originalFileName);
        List<String> reservedNames = Arrays.asList(
                "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5",
                "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4",
                "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
        );

        if (reservedNames.contains(fileNameWithoutExtension.toUpperCase())) {
            log.warn("예약된 파일명입니다: {}", originalFileName);
            throw new BusinessException(ErrorCode.FILE_NAME_INVALID,
                    "예약된 파일명은 사용할 수 없습니다: " + originalFileName);
        }
    }

    private void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            log.warn("파일 크기가 0입니다.");
            throw new BusinessException(ErrorCode.FILE_EMPTY, "파일이 비어있습니다.");
        }

        if (fileSize > maxFileSize) {
            log.warn("파일 크기가 제한을 초과했습니다: {} > {}", fileSize, maxFileSize);
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED,
                    String.format("파일 크기가 제한을 초과했습니다. (최대: %.1fMB)", maxFileSize / 1024.0 / 1024.0));
        }
    }

    private void validateFileExtension(String originalFileName) {
        String extension = extractExtension(originalFileName);

        if (!StringUtils.hasText(extension)) {
            log.warn("파일 확장자가 없습니다: {}", originalFileName);
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, "파일 확장자가 없습니다.");
        }

        if (DANGEROUS_EXTENSIONS.contains(extension.toLowerCase())) {
            log.warn("위험한 파일 확장자입니다: {}", extension);
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE,
                    "보안상 허용되지 않는 파일 형식입니다: " + extension);
        }

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.warn("허용되지 않는 파일 확장자입니다: {}", extension);
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE,
                    "허용되지 않는 파일 확장자입니다: " + extension +
                            ". 허용된 확장자: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private void validateContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            log.warn("콘텐츠 타입을 확인할 수 없습니다.");
            return;
        }

        List<String> dangerousContentTypes = Arrays.asList(
                "application/x-msdownload",
                "application/x-executable",
                "application/x-bat"
        );

        if (dangerousContentTypes.contains(contentType.toLowerCase())) {
            log.warn("위험한 콘텐츠 타입입니다: {}", contentType);
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE,
                    "보안상 허용되지 않는 파일 형식입니다.");
        }
    }

    public String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 && dotIndex < fileName.length() - 1
                ? fileName.substring(dotIndex + 1)
                : "";
    }

    private String getFileNameWithoutExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    public List<String> getAllowedExtensions() {
        return ALLOWED_EXTENSIONS;
    }

    public double getMaxFileSizeInMB() {
        return maxFileSize / 1024.0 / 1024.0;
    }
}