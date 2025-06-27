package com.example.j_backend.web.file.dto;

import com.example.j_backend.web.file.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FileMetadataResponse {
    private Long id;
    private String originalFileName;
    private String storedFileName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadTime;
    private String description;
    private String downloadUrl; // 파일 다운로드 URL 추가

    public static FileMetadataResponse from(FileMetadata fileMetadata, String downloadUrl) {
        return FileMetadataResponse.builder()
                .id(fileMetadata.getId())
                .originalFileName(fileMetadata.getOriginalFileName())
                .storedFileName(fileMetadata.getStoredFileName())
                .fileSize(fileMetadata.getFileSize())
                .contentType(fileMetadata.getContentType())
                .uploadTime(fileMetadata.getUploadTime())
                .description(fileMetadata.getDescription())
                .downloadUrl(downloadUrl)
                .build();
    }
}