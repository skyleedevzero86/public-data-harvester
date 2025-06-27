package com.antock.web.file.repository;

import com.antock.web.file.domain.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    // 원본 파일명 또는 설명으로 검색
    List<FileMetadata> findByOriginalFileNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String fileNameKeyword, String descriptionKeyword);

    // 저장된 파일명으로 검색
    Optional<FileMetadata> findByStoredFileName(String storedFileName);
}