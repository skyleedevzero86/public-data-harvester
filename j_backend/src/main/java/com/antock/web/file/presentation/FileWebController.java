package com.antock.web.file.presentation;

import com.antock.api.file.application.dto.FileResponse;
import com.antock.api.file.application.dto.FileUpdateCommand;
import com.antock.api.file.application.dto.FileUploadCommand;
import com.antock.api.file.application.service.FileApplicationService;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/web/files")
@RequiredArgsConstructor
public class FileWebController {

    private final FileApplicationService fileApplicationService;

    @GetMapping("/upload")
    public String uploadForm() {
        return "file/uploadForm";
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @CurrentUser AuthenticatedUser user,
            @RequestParam(value = "description", required = false) String description,
            RedirectAttributes redirectAttributes) {
        try {

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "로그인 후 이용해 주세요.");
                return "redirect:/members/login";
            }

            FileUploadCommand command = FileUploadCommand.builder()
                    .file(file)
                    .description(description)
                    .uploaderId(user.getId())
                    .uploaderName(user.getUsername())
                    .build();
            fileApplicationService.uploadFile(command);
            redirectAttributes.addFlashAttribute("message", "파일 업로드 성공!");
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "파일 업로드 실패: " + e.getMessage());
        }
        return "redirect:/web/files";
    }

    @GetMapping
    public String listFiles(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        try {
            List<FileResponse> files = keyword != null && !keyword.trim().isEmpty()
                    ? fileApplicationService.searchFiles(keyword)
                    : fileApplicationService.getAllFiles();

            model.addAttribute("files", files);
            if (keyword != null) {
                model.addAttribute("keyword", keyword);
            }
        } catch (Exception e) {
            log.error("파일 목록 조회 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 목록을 불러오는데 실패했습니다: " + e.getMessage());
        }
        return "file/fileList";
    }

    @GetMapping("/{id}")
    public String getFileDetails(@PathVariable Long id, Model model) {
        try {
            FileResponse file = fileApplicationService.getFileById(id);
            model.addAttribute("file", file);
        } catch (Exception e) {
            log.error("파일 상세 조회 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 상세 정보를 불러오는데 실패했습니다: " + e.getMessage());
        }
        return "file/fileDetails";
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

    @GetMapping("/edit/{id}")
    public String editFileForm(@PathVariable Long id, Model model) {
        try {
            FileResponse file = fileApplicationService.getFileById(id);
            model.addAttribute("file", file);
        } catch (Exception e) {
            log.error("파일 수정 폼 조회 실패: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 정보를 불러오는데 실패했습니다: " + e.getMessage());
            return "redirect:/web/files";
        }
        return "file/editFile";
    }

    @PostMapping("/update/{id}")
    public String updateFile(
            @PathVariable Long id,
            @RequestParam("description") String description,
            RedirectAttributes redirectAttributes) {
        try {
            FileUpdateCommand command = FileUpdateCommand.builder()
                    .id(id)
                    .description(description)
                    .build();

            fileApplicationService.updateFileDescription(command);
            redirectAttributes.addFlashAttribute("message", "파일 정보 업데이트 성공!");
        } catch (Exception e) {
            log.error("파일 업데이트 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "파일 정보 업데이트 실패: " + e.getMessage());
        }
        return "redirect:/web/files";
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fileApplicationService.deleteFile(id);
            redirectAttributes.addFlashAttribute("message", "파일 삭제 성공!");
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "파일 삭제 실패: " + e.getMessage());
        }
        return "redirect:/web/files";
    }
}