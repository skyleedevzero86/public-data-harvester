package com.antock.api.file.application.service;

import com.antock.api.file.application.dto.FileResponse;
import com.antock.api.file.application.dto.FileUpdateCommand;
import com.antock.api.file.application.dto.FileUploadCommand;
import com.antock.api.file.domain.File;
import com.antock.api.file.domain.vo.FileContent;
import com.antock.api.file.domain.vo.FileDescription;
import com.antock.api.file.domain.vo.FileMetadata;
import com.antock.api.file.infrastructure.FileRepository;
import com.antock.api.file.infrastructure.storage.FileStorageStrategy;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileApplicationService {

    private final FileRepository fileRepository;
    private final FileValidationService fileValidationService;
    private final FileStorageStrategy fileStorageStrategy;
    private final MemberRepository memberRepository;

    @Transactional
    public FileResponse uploadFile(FileUploadCommand command) {
        try {
            log.info("파일 업로드 시작: {}", command.getFile().getOriginalFilename());

            fileValidationService.validateUploadFile(command.getFile());

            String storedFileName = UUID.randomUUID().toString() + "_" + command.getFile().getOriginalFilename();

            Member uploader = null;
            if (command.getUploaderId() != null) {
                uploader = memberRepository.findById(command.getUploaderId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            }

            File file = File.builder()
                    .metadata(FileMetadata.of(
                            command.getFile().getOriginalFilename(),
                            storedFileName,
                            command.getFile().getContentType()
                    ))
                    .content(FileContent.of(command.getFile().getSize()))
                    .description(FileDescription.of(command.getDescription()))
                    .uploaderName(command.getUploaderName())
                    .uploader(uploader)
                    .uploadTime(LocalDateTime.now())
                    .lastModifiedTime(LocalDateTime.now())
                    .build();

            fileRepository.save(file);

            fileStorageStrategy.uploadFile(
                    command.getFile(),
                    storedFileName
            );

            File savedFile = fileRepository.save(file);
            String downloadUrl = generateDownloadUrl(savedFile.getMetadata().getStoredFileName());

            log.info("파일 업로드 완료: ID={}", savedFile.getId());
            return FileResponse.from(savedFile, downloadUrl);

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getAllFiles() {
        return fileRepository.findAll().stream()
                .map(file -> {
                    String downloadUrl = generateDownloadUrl(file.getMetadata().getStoredFileName());
                    return FileResponse.from(file, downloadUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FileResponse getFileById(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + id));

        String downloadUrl = generateDownloadUrl(file.getMetadata().getStoredFileName());
        return FileResponse.from(file, downloadUrl);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> searchFiles(String keyword) {
        return fileRepository.findByKeyword(keyword).stream()
                .map(file -> {
                    String downloadUrl = generateDownloadUrl(file.getMetadata().getStoredFileName());
                    return FileResponse.from(file, downloadUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public FileResponse updateFileDescription(FileUpdateCommand command) {
        File file = fileRepository.findById(command.getId())
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + command.getId()));

        file.updateDescription(command.getDescription());
        File updatedFile = fileRepository.save(file);

        String downloadUrl = generateDownloadUrl(updatedFile.getMetadata().getStoredFileName());
        return FileResponse.from(updatedFile, downloadUrl);
    }

    @Transactional
    public void deleteFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + id));

        try {
            fileStorageStrategy.deleteFile(file.getMetadata().getStoredFileName());

            fileRepository.delete(file);

            log.info("파일 삭제 완료: ID={}", id);
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public InputStreamResource downloadFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + id));

        try {
            return fileStorageStrategy.downloadFile(file.getMetadata().getStoredFileName());
        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 다운로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private String generateDownloadUrl(String storedFileName) {
        try {
            return fileStorageStrategy.getDownloadUrl(storedFileName);
        } catch (Exception e) {
            log.warn("다운로드 URL 생성 실패: {}", storedFileName, e);
            return null;
        }
    }
}