package com.antock.api.file.application.dto;

import com.antock.api.file.domain.File;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String originalFileName;
    private String storedFileName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadTime;
    private LocalDateTime lastModifiedTime;
    private String description;
    private String downloadUrl;

    public static FileResponse from(File file, String downloadUrl) {
        return FileResponse.builder()
                .id(file.getId())
                .originalFileName(file.getMetadata().getOriginalFileName())
                .storedFileName(file.getMetadata().getStoredFileName())
                .fileSize(file.getContent().getFileSize())
                .contentType(file.getMetadata().getContentType())
                .uploadTime(file.getUploadTime())
                .lastModifiedTime(file.getLastModifiedTime())
                .description(file.getDescription().getDescription())
                .downloadUrl(downloadUrl)
                .build();
    }

    public Date getUploadTimeAsDate() {
        if (uploadTime == null) return null;
        return Date.from(uploadTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public Date getLastModifiedTimeAsDate() {
        if (lastModifiedTime == null) return null;
        return Date.from(lastModifiedTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public double getFileSizeInMB() {
        return fileSize / (1024.0 * 1024.0);
    }
}