package com.antock.api.file.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileContent {

    @Column(nullable = false)
    private Long fileSize;

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    public static FileContent of(Long fileSize) {
        validateFileSize(fileSize);
        return FileContent.builder()
                .fileSize(fileSize)
                .build();
    }

    private static void validateFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            throw new IllegalArgumentException("파일 크기는 0보다 커야 합니다.");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 제한을 초과했습니다. (최대 100MB)");
        }
    }

    public double getFileSizeInMB() {
        return fileSize / (1024.0 * 1024.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileContent that = (FileContent) o;
        return Objects.equals(fileSize, that.fileSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSize);
    }
}