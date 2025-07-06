package com.antock.api.file.domain;

import com.antock.api.file.domain.vo.FileDescription;
import com.antock.api.file.domain.vo.FileMetadata;
import com.antock.api.file.domain.vo.FileContent;
import com.antock.api.member.domain.Member;
import com.antock.global.common.entity.BaseTimeEntity;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File extends BaseTimeEntity {

    @Embedded
    private FileMetadata metadata;

    @Embedded
    private FileContent content;

    @Embedded
    private FileDescription description;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @Column(nullable = false)
    private LocalDateTime lastModifiedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private Member uploader;

    @Column(length = 50)
    private String uploaderName;

    public static File create(String originalFileName, String contentType,
                              Long fileSize, String description) {
        String storedFileName = UUID.randomUUID().toString() +
                extractExtension(originalFileName);

        return File.builder()
                .metadata(FileMetadata.of(originalFileName, storedFileName, contentType))
                .content(FileContent.of(fileSize))
                .description(FileDescription.of(description))
                .uploadTime(LocalDateTime.now())
                .lastModifiedTime(LocalDateTime.now())
                .build();
    }

    public void updateDescription(String newDescription) {
        this.description = FileDescription.of(newDescription);
        this.lastModifiedTime = LocalDateTime.now();
    }

    private static String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }
}