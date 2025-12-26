package com.antock.api.file.application.service;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileValidationService 테스트")
class FileValidationServiceTest {

    @InjectMocks
    private FileValidationService fileValidationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileValidationService, "maxFileSize", 10485760L);
        ReflectionTestUtils.setField(fileValidationService, "maxFilenameLength", 255);
    }

    @Test
    @DisplayName("파일 검증 성공")
    void validateUploadFile_success() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("text/plain");

        fileValidationService.validateUploadFile(file);
    }

    @Test
    @DisplayName("파일이 비어있음")
    void validateUploadFile_empty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> fileValidationService.validateUploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_EMPTY);
    }

    @Test
    @DisplayName("파일명이 없음")
    void validateUploadFile_noFilename() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(null);

        assertThatThrownBy(() -> fileValidationService.validateUploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NAME_INVALID);
    }

    @Test
    @DisplayName("파일 크기 초과")
    void validateUploadFile_sizeExceeded() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getSize()).thenReturn(20000000L);

        assertThatThrownBy(() -> fileValidationService.validateUploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_SIZE_EXCEEDED);
    }

    @Test
    @DisplayName("위험한 확장자")
    void validateUploadFile_dangerousExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.exe");
        when(file.getSize()).thenReturn(1000L);

        assertThatThrownBy(() -> fileValidationService.validateUploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);
    }

    @Test
    @DisplayName("허용되지 않는 확장자")
    void validateUploadFile_invalidExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.xyz");
        when(file.getSize()).thenReturn(1000L);

        assertThatThrownBy(() -> fileValidationService.validateUploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);
    }

    @Test
    @DisplayName("파일명에 특수문자 포함")
    void validateUploadFile_invalidFilename() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test<file>.txt");
        when(file.getSize()).thenReturn(1000L);

        assertThatThrownBy(() -> fileValidationService.validateUploadFile(file))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NAME_INVALID);
    }

    @Test
    @DisplayName("확장자 추출")
    void extractExtension() {
        assertThat(fileValidationService.extractExtension("test.txt")).isEqualTo("txt");
        assertThat(fileValidationService.extractExtension("test.file.pdf")).isEqualTo("pdf");
        assertThat(fileValidationService.extractExtension("test")).isEqualTo("");
        assertThat(fileValidationService.extractExtension(null)).isEqualTo("");
    }

    @Test
    @DisplayName("허용된 확장자 목록 조회")
    void getAllowedExtensions() {
        assertThat(fileValidationService.getAllowedExtensions()).isNotEmpty();
    }

    @Test
    @DisplayName("최대 파일 크기 조회")
    void getMaxFileSizeInMB() {
        assertThat(fileValidationService.getMaxFileSizeInMB()).isGreaterThan(0);
    }
}

