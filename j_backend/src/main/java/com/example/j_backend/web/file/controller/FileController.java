package com.example.j_backend.web.file.controller;

import com.example.j_backend.web.file.dto.FileMetadataResponse;
import com.example.j_backend.web.file.dto.FileUploadRequest;
import com.example.j_backend.web.file.service.FileService;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // 파일 업로드 폼 페이지
    @GetMapping("/uploadForm")
    public String uploadForm() {
        return "file/uploadForm"; // /WEB-INF/views/uploadForm.jsp
    }

    // 파일 업로드 처리 (CREATE)
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

    // 모든 파일 조회 및 검색 (READ All & Search)
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
        return "file/fileList"; // /WEB-INF/views/fileList.jsp
    }

    // 특정 파일 정보 조회 (READ One) - (선택 사항, 상세 보기 페이지로 연결)
    @GetMapping("/{id}")
    public String getFileDetails(@PathVariable Long id, Model model) {
        try {
            FileMetadataResponse file = fileService.getFileById(id);
            model.addAttribute("file", file);
        } catch (Exception e) {
            model.addAttribute("error", "파일 상세 정보를 불러오는데 실패했습니다: " + e.getMessage());
        }
        return "file/fileDetails"; // /WEB-INF/views/fileDetails.jsp
    }

    // 파일 다운로드
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) {
        try {
            FileMetadataResponse fileMetadata = fileService.getFileById(id);
            InputStreamResource resource = fileService.downloadFile(id);

            String encodedFileName = URLEncoder.encode(fileMetadata.getOriginalFileName(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.parseMediaType(fileMetadata.getContentType()))
                    .contentLength(fileMetadata.getFileSize())
                    .body(resource);
        } catch (Exception e) {
            // 예외 발생 시 적절한 HTTP 상태 코드와 메시지 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // 파일 설명 업데이트 폼 (UPDATE)
    @GetMapping("/edit/{id}")
    public String editFileForm(@PathVariable Long id, Model model) {
        try {
            FileMetadataResponse file = fileService.getFileById(id);
            model.addAttribute("file", file);
        } catch (Exception e) {
            model.addAttribute("error", "파일 정보를 불러오는데 실패했습니다: " + e.getMessage());
            return "redirect:/files";
        }
        return "file/editFile"; // /WEB-INF/views/editFile.jsp
    }

    // 파일 설명 업데이트 처리 (UPDATE)
    @PostMapping("/update/{id}")
    public String updateFile(@PathVariable Long id, @RequestParam("description") String description, RedirectAttributes redirectAttributes) {
        try {
            fileService.updateFileDescription(id, description);
            redirectAttributes.addFlashAttribute("message", "파일 정보 업데이트 성공!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "파일 정보 업데이트 실패: " + e.getMessage());
        }
        return "redirect:/files";
    }

    // 파일 삭제 (DELETE)
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