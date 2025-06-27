package com.antock.web.file.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "file")
@Getter
@Setter
@NoArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName; // 원본 파일명

    @Column(nullable = false, unique = true)
    private String storedFileName; // MinIO에 저장된 파일명 (UUID 등)

    @Column(nullable = false)
    private Long fileSize; // 파일 크기 (바이트)

    @Column(nullable = false)
    private String contentType; // 파일 MIME 타입

    @Column(nullable = false)
    private LocalDateTime uploadTime; // 업로드 시간

    // 검색을 위한 추가 필드 (예: 파일 설명)
    private String description;

    @Builder
    public FileMetadata(String originalFileName, String storedFileName, Long fileSize, String contentType, LocalDateTime uploadTime, String description) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadTime = uploadTime;
        this.description = description;
    }
}