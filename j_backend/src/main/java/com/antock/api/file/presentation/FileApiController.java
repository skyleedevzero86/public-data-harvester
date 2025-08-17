package com.antock.api.file.presentation;

import com.antock.api.file.application.dto.FileResponse;
import com.antock.api.file.application.dto.FileUpdateCommand;
import com.antock.api.file.application.dto.FileUploadCommand;
import com.antock.api.file.application.service.FileApplicationService;
import com.antock.api.file.application.service.FileValidationService;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileApiController {

    private final FileApplicationService fileApplicationService;
    private final FileValidationService fileValidationService;

    @PostMapping("/upload")
    public ApiResponse<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @CurrentUser AuthenticatedUser user) {

        try {
            fileValidationService.validateUploadFile(file);

            FileUploadCommand command = FileUploadCommand.builder()
                    .file(file)
                    .description(description)
                    .uploaderId(user.getId())
                    .uploaderName(user.getNickname())
                    .username(user.getUsername())
                    .build();

            FileResponse response = fileApplicationService.uploadFile(command);

            log.info("File uploaded successfully: {}", response.getOriginalFileName());
            return ApiResponse.success(response, "파일이 성공적으로 업로드되었습니다.");

        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return ApiResponse.error("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/{fileId}")
    public ApiResponse<FileResponse> getFileInfo(@PathVariable Long fileId) {
        try {
            FileResponse response = fileApplicationService.getFileById(fileId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Failed to get file info for ID {}: {}", fileId, e.getMessage(), e);
            return ApiResponse.error("파일 정보 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<FileResponse>> getFileList(Pageable pageable) {
        try {
            List<FileResponse> files = fileApplicationService.getAllFiles();
            return ApiResponse.success(files);
        } catch (Exception e) {
            log.error("Failed to get file list: {}", e.getMessage(), e);
            return ApiResponse.error("파일 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{fileId}")
    public ApiResponse<FileResponse> updateFile(
            @PathVariable Long fileId,
            @Valid @RequestBody FileUpdateCommand command,
            @CurrentUser AuthenticatedUser user) {

        try {
            command.setId(fileId);
            command.setUsername(user.getUsername());

            FileResponse response = fileApplicationService.updateFileDescription(command);
            log.info("File description updated successfully: {}", fileId);
            return ApiResponse.success(response, "파일 설명이 성공적으로 수정되었습니다.");

        } catch (Exception e) {
            log.error("Failed to update file description for ID {}: {}", fileId, e.getMessage(), e);
            return ApiResponse.error("파일 설명 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> deleteFile(
            @PathVariable Long fileId,
            @CurrentUser AuthenticatedUser user) {

        try {
            fileApplicationService.deleteFile(fileId);
            log.info("File deleted successfully: {}", fileId);
            return ApiResponse.successVoid("파일이 성공적으로 삭제되었습니다.");

        } catch (Exception e) {
            log.error("Failed to delete file with ID {}: {}", fileId, e.getMessage(), e);
            return ApiResponse.error("파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            InputStreamResource resource = fileApplicationService.downloadFile(fileId);
            FileResponse fileInfo = fileApplicationService.getFileById(fileId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .contentLength(fileInfo.getFileSize())
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download file with ID {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ApiResponse<List<FileResponse>> searchFiles(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "extension", required = false) String extension) {

        try {
            List<FileResponse> files = fileApplicationService.searchFiles(keyword);
            return ApiResponse.success(files);
        } catch (Exception e) {
            log.error("Failed to search files: {}", e.getMessage(), e);
            return ApiResponse.error("파일 검색에 실패했습니다: " + e.getMessage());
        }
    }
}
