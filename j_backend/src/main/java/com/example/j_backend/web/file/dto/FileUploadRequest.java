package com.example.j_backend.web.file.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class FileUploadRequest {
    private MultipartFile file;
    private String description; // 파일에 대한 설명
}