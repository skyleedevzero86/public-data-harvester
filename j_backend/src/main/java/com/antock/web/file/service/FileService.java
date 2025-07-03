package com.antock.web.file.service;

import com.antock.web.file.domain.FileMetadata;
import com.antock.web.file.dto.FileMetadataResponse;
import com.antock.web.file.dto.FileUploadRequest;
import com.antock.web.file.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final MinioService minioService;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip");

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB

    @Transactional
    public FileMetadataResponse uploadFile(FileUploadRequest request) throws Exception {
        log.info("파일 업로드 시작: {}", request.getFile().getOriginalFilename());

        MultipartFile file = request.getFile();

        try {
            // 1. 파일 유효성 검사
            validateFile(file);

            // 2. MinIO에 저장될 고유한 파일명 생성
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;
            log.info("생성된 저장 파일명: {}", storedFileName);

            // 3. MinIO에 파일 업로드
            log.info("MinIO 업로드 시작");
            minioService.uploadFile(file, storedFileName);
            log.info("MinIO 업로드 완료");

            // 4. 파일 메타데이터를 데이터베이스에 저장
            log.info("데이터베이스 저장 시작");
            FileMetadata fileMetadata = FileMetadata.builder()
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadTime(LocalDateTime.now())
                    .description(request.getDescription())
                    .build();

            FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);
            log.info("데이터베이스 저장 완료. ID: {}", savedMetadata.getId());

            // 5. 다운로드 URL 생성 및 반환
            String downloadUrl = minioService.getPreSignedUrl(savedMetadata.getStoredFileName());
            log.info("파일 업로드 전체 과정 완료");

            return FileMetadataResponse.from(savedMetadata, downloadUrl);

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "파일 크기가 너무 큽니다. (최대 " + MAX_FILE_SIZE / (1024 * 1024) + "MB)");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "원본 파일명을 확인할 수 없습니다.");
        }
        String fileExtension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "허용되지 않는 파일 확장자입니다: " + fileExtension);
        }
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getAllFiles() throws Exception {
        return fileMetadataRepository.findAll().stream()
                .map(fileMetadata -> {
                    try {
                        String downloadUrl = minioService.getPreSignedUrl(fileMetadata.getStoredFileName());
                        return FileMetadataResponse.from(fileMetadata, downloadUrl);
                    } catch (Exception e) {
                        log.warn("다운로드 URL 생성 실패: {}", fileMetadata.getStoredFileName(), e);
                        return FileMetadataResponse.from(fileMetadata, "");
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileMetadataResponse getFileById(Long id) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));
        String downloadUrl = minioService.getPreSignedUrl(fileMetadata.getStoredFileName());
        return FileMetadataResponse.from(fileMetadata, downloadUrl);
    }

    @Transactional(readOnly = true)
    public InputStreamResource downloadFile(Long id) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));
        return minioService.downloadFile(fileMetadata.getStoredFileName());
    }

    @Transactional
    public FileMetadataResponse updateFileDescription(Long id, String newDescription) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));
        fileMetadata.setDescription(newDescription);
        FileMetadata updatedMetadata = fileMetadataRepository.save(fileMetadata);
        String downloadUrl = minioService.getPreSignedUrl(updatedMetadata.getStoredFileName());
        return FileMetadataResponse.from(updatedMetadata, downloadUrl);
    }

    @Transactional
    public void deleteFile(Long id) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));

        // 1. MinIO에서 파일 삭제
        minioService.deleteFile(fileMetadata.getStoredFileName());

        // 2. 데이터베이스에서 메타데이터 삭제
        fileMetadataRepository.delete(fileMetadata);
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponse> searchFiles(String keyword) throws Exception {
        List<FileMetadata> foundFiles = fileMetadataRepository
                .findByOriginalFileNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        return foundFiles.stream()
                .map(fileMetadata -> {
                    try {
                        String downloadUrl = minioService.getPreSignedUrl(fileMetadata.getStoredFileName());
                        return FileMetadataResponse.from(fileMetadata, downloadUrl);
                    } catch (Exception e) {
                        return FileMetadataResponse.from(fileMetadata, "");
                    }
                })
                .collect(Collectors.toList());
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}