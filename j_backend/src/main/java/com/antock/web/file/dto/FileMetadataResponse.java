package com.antock.web.file.dto;

import com.antock.web.file.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
    private String downloadUrl;

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

    public Date getUploadTimeAsDate() {
        return Date.from(uploadTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}