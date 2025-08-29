package com.antock.api.file.presentation;

import com.antock.api.file.application.dto.FileResponse;
import com.antock.api.file.application.dto.FileUpdateCommand;
import com.antock.api.file.application.dto.FileUploadCommand;
import com.antock.api.file.application.service.FileApplicationService;
import com.antock.api.file.application.service.FileValidationService;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "File Management", description = "파일 업로드, 다운로드 및 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class FileApiController {

    private final FileApplicationService fileApplicationService;
    private final FileValidationService fileValidationService;

    @Operation(summary = "파일 업로드", description = """
            파일을 서버에 업로드합니다.

            ### 기능
            - 다양한 형식의 파일 업로드 지원
            - 파일 크기 및 형식 검증
            - MinIO 스토리지에 안전하게 저장
            - 업로드한 사용자 정보 자동 기록

            ### 제약사항
            - 최대 파일 크기: 50MB
            - 허용된 파일 형식만 업로드 가능
            - 로그인된 사용자만 업로드 가능
            """, tags = { "File Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 업로드 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일 크기 초과, 지원하지 않는 형식 등)", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "413", description = "파일 크기 초과", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @PostMapping("/upload")
    public com.antock.global.common.response.ApiResponse<FileResponse> uploadFile(
            @Parameter(description = "업로드할 파일", required = true, content = @Content(mediaType = "multipart/form-data")) @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 설명 (선택사항)", example = "프로젝트 관련 문서", in = ParameterIn.QUERY) @RequestParam(value = "description", required = false) String description,
            @Parameter(hidden = true) @CurrentUser AuthenticatedUser user) {

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
            return com.antock.global.common.response.ApiResponse.success(response, "파일이 성공적으로 업로드되었습니다.");

        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return com.antock.global.common.response.ApiResponse.error("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "파일 정보 조회", description = """
            파일 ID로 파일의 상세 정보를 조회합니다.

            ### 반환 정보
            - 파일명, 크기, 형식
            - 업로드 일시 및 업로드한 사용자
            - 파일 설명
            - 다운로드 가능 여부
            """, tags = { "File Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 정보 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileResponse.class))),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{fileId}")
    public com.antock.global.common.response.ApiResponse<FileResponse> getFileInfo(
            @Parameter(description = "파일 고유 ID", example = "1", required = true) @PathVariable Long fileId) {
        try {
            FileResponse response = fileApplicationService.getFileById(fileId);
            return com.antock.global.common.response.ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Failed to get file info for ID {}: {}", fileId, e.getMessage(), e);
            return com.antock.global.common.response.ApiResponse.error("파일 정보 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "파일 목록 조회", description = """
            전체 파일 목록을 조회합니다.

            ### 기능
            - 전체 파일 목록 반환
            - 파일 기본 정보 포함
            - 업로드 일시 순으로 정렬
            """, tags = { "File Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 목록 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping
    public com.antock.global.common.response.ApiResponse<List<FileResponse>> getFileList(Pageable pageable) {
        try {
            List<FileResponse> files = fileApplicationService.getAllFiles();
            return com.antock.global.common.response.ApiResponse.success(files);
        } catch (Exception e) {
            log.error("Failed to get file list: {}", e.getMessage(), e);
            return com.antock.global.common.response.ApiResponse.error("파일 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "파일 설명 수정", description = """
            파일의 설명을 수정합니다.

            ### 기능
            - 파일 설명 변경
            - 수정자 정보 자동 기록
            - 수정 일시 업데이트

            ### 제약사항
            - 로그인된 사용자만 수정 가능
            - 파일 소유자만 수정 가능 (관리자 제외)
            """, tags = { "File Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 설명 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FileResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (파일 소유자 아님)", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @PutMapping("/{fileId}")
    public com.antock.global.common.response.ApiResponse<FileResponse> updateFile(
            @Parameter(description = "파일 고유 ID", example = "1", required = true) @PathVariable Long fileId,
            @Valid @RequestBody FileUpdateCommand command,
            @Parameter(hidden = true) @CurrentUser AuthenticatedUser user) {

        try {
            command.setId(fileId);
            command.setUsername(user.getUsername());

            FileResponse response = fileApplicationService.updateFileDescription(command);
            log.info("File description updated successfully: {}", fileId);
            return com.antock.global.common.response.ApiResponse.success(response, "파일 설명이 성공적으로 수정되었습니다.");

        } catch (Exception e) {
            log.error("Failed to update file description for ID {}: {}", fileId, e.getMessage(), e);
            return com.antock.global.common.response.ApiResponse.error("파일 설명 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "파일 삭제", description = """
            파일을 완전히 삭제합니다.

            ### 기능
            - 파일 데이터베이스 및 스토리지에서 완전 삭제
            - 대사 불가능한 영구 삭제

            ### 제약사항
            - 로그인된 사용자만 삭제 가능
            - 파일 소유자만 삭제 가능 (관리자 제외)
            - 삭제 후 복구 불가능
            """, tags = { "File Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (파일 소유자 아님)", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @DeleteMapping("/{fileId}")
    public com.antock.global.common.response.ApiResponse<Void> deleteFile(
            @Parameter(description = "파일 고유 ID", example = "1", required = true) @PathVariable Long fileId,
            @Parameter(hidden = true) @CurrentUser AuthenticatedUser user) {

        try {
            fileApplicationService.deleteFile(fileId);
            log.info("File deleted successfully: {}", fileId);
            return com.antock.global.common.response.ApiResponse.successVoid("파일이 성공적으로 삭제되었습니다.");

        } catch (Exception e) {
            log.error("Failed to delete file with ID {}: {}", fileId, e.getMessage(), e);
            return com.antock.global.common.response.ApiResponse.error("파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    @Operation(summary = "파일 다운로드", description = """
            파일을 다운로드합니다.

            ### 기능
            - 원본 파일명으로 다운로드
            - 원본 컨텐츠 타입 유지
            - 전체 파일 크기 지원

            ### 제약사항
            - 로그인된 사용자만 다운로드 가능
            - 파일 조회 권한이 있는 사용자만 다운로드 가능
            """, tags = { "File Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공", content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다운로드 권한 없음)"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "파일 고유 ID", example = "1", required = true) @PathVariable Long fileId) {
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

    @Operation(summary = "파일 검색", description = """
            파일명이나 설명으로 파일을 검색합니다.

            ### 기능
            - 파일명 부분 일치 검색
            - 파일 설명 부분 일치 검색
            - 대소문자 구분 없이 검색
            - 확장자별 필터링 (선택사항)

            ### 주의사항
            - 키워드와 확장자 중 하나 이상 입력 필요
            - 결과는 업로드 일시 순으로 정렬
            """, tags = { "File Management" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 검색 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 조건", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/search")
    public com.antock.global.common.response.ApiResponse<List<FileResponse>> searchFiles(
            @Parameter(description = "검색 키워드 (파일명 또는 설명에서 검색)", example = "문서", in = ParameterIn.QUERY) @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "파일 확장자 (선택사항)", example = "pdf", in = ParameterIn.QUERY) @RequestParam(value = "extension", required = false) String extension) {

        try {
            List<FileResponse> files = fileApplicationService.searchFiles(keyword);
            return com.antock.global.common.response.ApiResponse.success(files);
        } catch (Exception e) {
            log.error("Failed to search files: {}", e.getMessage(), e);
            return com.antock.global.common.response.ApiResponse.error("파일 검색에 실패했습니다: " + e.getMessage());
        }
    }
}
