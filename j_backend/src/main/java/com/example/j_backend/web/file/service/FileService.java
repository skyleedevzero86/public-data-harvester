package com.example.j_backend.web.file.service;

import com.example.j_backend.web.file.domain.FileMetadata;
import com.example.j_backend.web.file.dto.FileMetadataResponse;
import com.example.j_backend.web.file.dto.FileUploadRequest;
import com.example.j_backend.web.file.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final MinioService minioService;

    // 허용된 파일 확장자 목록 (방어 코딩)
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip"
    );

    // 최대 파일 크기 (예: 100MB)
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB

    /**
     * 파일 업로드 및 메타데이터 저장 (CREATE)
     *
     * @param request 파일 업로드 요청 DTO
     * @return 업로드된 파일 메타데이터 응답 DTO
     */
    @Transactional
    public FileMetadataResponse uploadFile(FileUploadRequest request) throws Exception {
        MultipartFile file = request.getFile();

        // 1. 파일 유효성 검사 (방어 코딩)
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다. (최대 " + MAX_FILE_SIZE / (1024 * 1024) + "MB)");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "원본 파일명을 확인할 수 없습니다.");
        }
        String fileExtension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "허용되지 않는 파일 확장자입니다: " + fileExtension);
        }

        // 2. MinIO에 저장될 고유한 파일명 생성
        String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

        // 3. MinIO에 파일 업로드
        minioService.uploadFile(file, storedFileName);

        // 4. 파일 메타데이터를 데이터베이스에 저장
        FileMetadata fileMetadata = FileMetadata.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .uploadTime(LocalDateTime.now())
                .description(request.getDescription())
                .build();

        FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);

        // 5. 다운로드 URL 생성 및 반환
        String downloadUrl = minioService.getPreSignedUrl(savedMetadata.getStoredFileName());
        return FileMetadataResponse.from(savedMetadata, downloadUrl);
    }

    /**
     * 모든 파일 메타데이터 조회 (READ All)
     *
     * @return 모든 파일 메타데이터 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getAllFiles() throws Exception {
        return fileMetadataRepository.findAll().stream()
                .map(fileMetadata -> {
                    try {
                        String downloadUrl = minioService.getPreSignedUrl(fileMetadata.getStoredFileName());
                        return FileMetadataResponse.from(fileMetadata, downloadUrl);
                    } catch (Exception e) {
                        // URL 생성 실패 시 null 또는 예외 처리 (여기서는 임시로 빈 문자열)
                        return FileMetadataResponse.from(fileMetadata, "");
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 파일 메타데이터 조회 (READ One)
     *
     * @param id 파일 ID
     * @return 파일 메타데이터 응답 DTO
     */
    @Transactional(readOnly = true)
    public FileMetadataResponse getFileById(Long id) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));
        String downloadUrl = minioService.getPreSignedUrl(fileMetadata.getStoredFileName());
        return FileMetadataResponse.from(fileMetadata, downloadUrl);
    }

    /**
     * 파일 다운로드
     *
     * @param id 파일 ID
     * @return 파일 데이터 (InputStreamResource)
     */
    @Transactional(readOnly = true)
    public InputStreamResource downloadFile(Long id) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));
        return minioService.downloadFile(fileMetadata.getStoredFileName());
    }

    /**
     * 파일 메타데이터 업데이트 (UPDATE)
     * (현재는 설명만 업데이트 가능하도록 구현)
     *
     * @param id 파일 ID
     * @param newDescription 새로운 설명
     * @return 업데이트된 파일 메타데이터 응답 DTO
     */
    @Transactional
    public FileMetadataResponse updateFileDescription(Long id, String newDescription) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));
        fileMetadata.setDescription(newDescription);
        FileMetadata updatedMetadata = fileMetadataRepository.save(fileMetadata);
        String downloadUrl = minioService.getPreSignedUrl(updatedMetadata.getStoredFileName());
        return FileMetadataResponse.from(updatedMetadata, downloadUrl);
    }

    /**
     * 파일 삭제 (DELETE)
     * MinIO에서도 파일을 삭제하고, 데이터베이스에서도 메타데이터를 삭제합니다.
     *
     * @param id 파일 ID
     */
    @Transactional
    public void deleteFile(Long id) throws Exception {
        FileMetadata fileMetadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다: " + id));

        // 1. MinIO에서 파일 삭제
        minioService.deleteFile(fileMetadata.getStoredFileName());

        // 2. 데이터베이스에서 메타데이터 삭제
        fileMetadataRepository.delete(fileMetadata);
    }

    /**
     * 파일 검색 (Search)
     *
     * @param keyword 검색 키워드 (원본 파일명 또는 설명에서 검색)
     * @return 검색된 파일 메타데이터 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponse> searchFiles(String keyword) throws Exception {
        List<FileMetadata> foundFiles = fileMetadataRepository.findByOriginalFileNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
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

    // 파일 확장자를 추출하는 헬퍼 메소드
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}