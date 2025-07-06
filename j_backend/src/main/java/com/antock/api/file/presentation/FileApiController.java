package com.antock.api.file.presentation;

import com.antock.api.file.application.dto.FileResponse;
import com.antock.api.file.application.dto.FileUpdateCommand;
import com.antock.api.file.application.dto.FileUploadCommand;
import com.antock.api.file.application.service.FileApplicationService;
import com.antock.global.config.CsvTemplateConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileApiController {

    private final FileApplicationService fileApplicationService;

    @Autowired
    private CsvTemplateConfig csvTemplateConfig;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        try {
            FileUploadCommand command = FileUploadCommand.builder()
                    .file(file)
                    .description(description)
                    .build();

            FileResponse response = fileApplicationService.uploadFile(command);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getAllFiles(
            @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            List<FileResponse> files = keyword != null && !keyword.trim().isEmpty()
                    ? fileApplicationService.searchFiles(keyword)
                    : fileApplicationService.getAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("파일 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFileById(@PathVariable Long id) {
        try {
            FileResponse file = fileApplicationService.getFileById(id);
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            log.error("파일 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) {
        try {
            FileResponse fileMetadata = fileApplicationService.getFileById(id);
            InputStreamResource resource = fileApplicationService.downloadFile(id);

            String encodedFileName = URLEncoder.encode(
                    fileMetadata.getOriginalFileName(),
                    StandardCharsets.UTF_8
            ).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.parseMediaType(fileMetadata.getContentType()))
                    .contentLength(fileMetadata.getFileSize())
                    .body(resource);
        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileResponse> updateFile(
            @PathVariable Long id,
            @RequestBody FileUpdateCommand command) {
        try {
            command.setId(id);
            FileResponse response = fileApplicationService.updateFileDescription(command);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("파일 업데이트 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        try {
            fileApplicationService.deleteFile(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/template")
    public ResponseEntity<Resource> downloadCsvTemplate() {
        Resource resource = new ClassPathResource("CSVFile/" + csvTemplateConfig.getTemplateName());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvTemplateConfig.getTemplateName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}