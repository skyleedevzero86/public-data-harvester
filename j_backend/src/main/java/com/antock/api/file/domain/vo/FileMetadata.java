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
public class FileMetadata {

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String storedFileName;

    @Column(nullable = false)
    private String contentType;

    public static FileMetadata of(String originalFileName, String storedFileName, String contentType) {
        validateFileName(originalFileName);
        validateContentType(contentType);

        return FileMetadata.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .contentType(contentType)
                .build();
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명은 필수입니다.");
        }
    }

    private static void validateContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("콘텐츠 타입은 필수입니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return Objects.equals(storedFileName, that.storedFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storedFileName);
    }
}