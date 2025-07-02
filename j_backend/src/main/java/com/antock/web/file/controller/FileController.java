package com.antock.web.file.controller;

import com.antock.web.file.dto.FileMetadataResponse;
import com.antock.web.file.dto.FileUploadRequest;
import com.antock.web.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/uploadForm")
    public String uploadForm() {
        return "file/uploadForm"; // /WEB-INF/views/uploadForm.jsp
    }

    @PostMapping("/upload")
    public String uploadFile(@ModelAttribute FileUploadRequest request, RedirectAttributes redirectAttributes) {
        try {
            fileService.uploadFile(request);
            redirectAttributes.addFlashAttribute("message", "파일 업로드 성공!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 실패: " + e.getMessage());
        }
        return "redirect:/files";
    }

    @GetMapping
    public String listFiles(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        try {
            List<FileMetadataResponse> files;
            if (keyword != null && !keyword.trim().isEmpty()) {
                files = fileService.searchFiles(keyword);
                model.addAttribute("keyword", keyword);
            } else {
                files = fileService.getAllFiles();
            }

            model.addAttribute("files", files);
        } catch (Exception e) {
            model.addAttribute("error", "파일 목록을 불러오는데 실패했습니다: " + e.getMessage());
        }
        return "file/fileList";
    }

    @GetMapping("/{id}")
    public String getFileDetails(@PathVariable Long id, Model model) {
        try {
            FileMetadataResponse file = fileService.getFileById(id);
            model.addAttribute("file", file);
        } catch (Exception e) {
            model.addAttribute("error", "파일 상세 정보를 불러오는데 실패했습니다: " + e.getMessage());
        }
        return "file/fileDetails";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) {
        try {
            FileMetadataResponse fileMetadata = fileService.getFileById(id);
            InputStreamResource resource = fileService.downloadFile(id);

            String encodedFileName = URLEncoder
                    .encode(fileMetadata.getOriginalFileName(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.parseMediaType(fileMetadata.getContentType()))
                    .contentLength(fileMetadata.getFileSize())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/edit/{id}")
    public String editFileForm(@PathVariable Long id, Model model) {
        try {
            FileMetadataResponse file = fileService.getFileById(id);
            model.addAttribute("file", file);
        } catch (Exception e) {
            model.addAttribute("error", "파일 정보를 불러오는데 실패했습니다: " + e.getMessage());
            return "redirect:/files";
        }
        return "file/editFile";
    }

    @PostMapping("/update/{id}")
    public String updateFile(@PathVariable Long id, @RequestParam("description") String description,
            RedirectAttributes redirectAttributes) {
        try {
            fileService.updateFileDescription(id, description);
            redirectAttributes.addFlashAttribute("message", "파일 정보 업데이트 성공!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "파일 정보 업데이트 실패: " + e.getMessage());
        }
        return "redirect:/files";
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fileService.deleteFile(id);
            redirectAttributes.addFlashAttribute("message", "파일 삭제 성공!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "파일 삭제 실패: " + e.getMessage());
        }
        return "redirect:/files";
    }
}