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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

        log.info("파일 업로드 요청: filename={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            fileValidationService.validateUploadFile(file);

            FileUploadCommand command = FileUploadCommand.builder()
                    .file(file)
                    .description(description)
                    .username(user.getUsername())
                    .build();

            FileResponse response = fileApplicationService.uploadFile(command);
            return ApiResponse.success(response, "파일 업로드가 완료되었습니다.");

        } catch (Exception e) {
            log.error("파일 업로드 실패", e);
            return ApiResponse.error("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/{fileId}")
    public ApiResponse<FileResponse> getFileInfo(@PathVariable Long fileId) {
        log.info("파일 정보 조회 요청 - ID: {}", fileId);

        try {
            FileResponse response = fileApplicationService.getFileInfo(fileId);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("파일 정보 조회 실패 - ID: {}", fileId, e);
            return ApiResponse.error("파일 정보를 찾을 수 없습니다.");
        }
    }

    @GetMapping
    public ApiResponse<Page<FileResponse>> getFileList(Pageable pageable) {
        log.info("파일 목록 조회 요청");

        try {
            Page<FileResponse> files = fileApplicationService.getFileList(pageable);
            return ApiResponse.success(files);
        } catch (Exception e) {
            log.error("파일 목록 조회 실패", e);
            return ApiResponse.error("파일 목록을 가져올 수 없습니다.");
        }
    }

    @PutMapping("/{fileId}")
    public ApiResponse<FileResponse> updateFile(
            @PathVariable Long fileId,
            @Valid @RequestBody FileUpdateCommand command,
            @CurrentUser AuthenticatedUser user) {

        log.info("파일 정보 수정 요청 - ID: {}", fileId);

        try {
            command.setUsername(user.getUsername());
            FileResponse response = fileApplicationService.updateFile(fileId, command);
            return ApiResponse.success(response, "파일 정보가 수정되었습니다.");
        } catch (Exception e) {
            log.error("파일 정보 수정 실패 - ID: {}", fileId, e);
            return ApiResponse.error("파일 정보 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> deleteFile(
            @PathVariable Long fileId,
            @CurrentUser AuthenticatedUser user) {

        log.info("파일 삭제 요청 - ID: {}", fileId);

        try {
            fileApplicationService.deleteFile(fileId, user.getUsername());
            return ApiResponse.successVoid("파일이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("파일 삭제 실패 - ID: {}", fileId, e);
            return ApiResponse.errorVoid("파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        log.info("파일 다운로드 요청 - ID: {}", fileId);

        try {
            FileResponse fileInfo = fileApplicationService.getFileInfo(fileId);
            Resource resource = fileApplicationService.downloadFile(fileId);

            String encodedFilename = URLEncoder.encode(fileInfo.getOriginalFileName(), StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);

        } catch (Exception e) {
            log.error("파일 다운로드 실패 - ID: {}", fileId, e);
            throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
        }
    }

    @GetMapping("/search")
    public ApiResponse<List<FileResponse>> searchFiles(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "extension", required = false) String extension) {

        log.info("파일 검색 요청 - keyword: {}, extension: {}", keyword, extension);

        try {
            List<FileResponse> files = fileApplicationService.searchFiles(keyword, extension);
            return ApiResponse.success(files);
        } catch (Exception e) {
            log.error("파일 검색 실패", e);
            return ApiResponse.error("파일 검색에 실패했습니다: " + e.getMessage());
        }
    }
}