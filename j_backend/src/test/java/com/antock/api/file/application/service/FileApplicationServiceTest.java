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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileApplicationService 테스트")
class FileApplicationServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileValidationService fileValidationService;

    @Mock
    private FileStorageStrategy fileStorageStrategy;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileApplicationService fileApplicationService;

    private File testFile;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testFile = File.builder()
                .metadata(FileMetadata.of("test.txt", "uuid_test.txt", "text/plain"))
                .content(FileContent.of(100L))
                .description(FileDescription.of("test description"))
                .uploadTime(LocalDateTime.now())
                .lastModifiedTime(LocalDateTime.now())
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testFile, "id", 1L);

        testMember = Member.builder()
                .username("testuser")
                .password("encoded")
                .nickname("test")
                .email("test@test.com")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(testMember, "id", 1L);
    }

    @Test
    @DisplayName("파일 업로드 성공")
    void uploadFile_success() {
        FileUploadCommand command = FileUploadCommand.builder()
                .file(multipartFile)
                .description("test")
                .uploaderId(1L)
                .uploaderName("testuser")
                .build();

        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getSize()).thenReturn(100L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(fileRepository.save(any(File.class))).thenReturn(testFile);
        try {
            when(fileStorageStrategy.getDownloadUrl(anyString())).thenReturn("http://example.com/file");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FileResponse response = fileApplicationService.uploadFile(command);

        assertThat(response).isNotNull();
        verify(fileValidationService).validateUploadFile(multipartFile);
        try {
            verify(fileStorageStrategy).uploadFile(any(MultipartFile.class), anyString());
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("파일 업로드 - 회원 없음")
    void uploadFile_memberNotFound() {
        FileUploadCommand command = FileUploadCommand.builder()
                .file(multipartFile)
                .uploaderId(999L)
                .build();

        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileApplicationService.uploadFile(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("파일 업로드 - 예외 발생")
    void uploadFile_exception() {
        FileUploadCommand command = FileUploadCommand.builder()
                .file(multipartFile)
                .build();

        doThrow(new RuntimeException("validation error")).when(fileValidationService).validateUploadFile(multipartFile);

        assertThatThrownBy(() -> fileApplicationService.uploadFile(command))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("모든 파일 조회")
    void getAllFiles() {
        List<File> files = Arrays.asList(testFile);
        when(fileRepository.findAll()).thenReturn(files);
        try {
            when(fileStorageStrategy.getDownloadUrl(anyString())).thenReturn("http://example.com/file");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<FileResponse> responses = fileApplicationService.getAllFiles();

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("파일 ID로 조회")
    void getFileById() {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        try {
            when(fileStorageStrategy.getDownloadUrl(anyString())).thenReturn("http://example.com/file");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FileResponse response = fileApplicationService.getFileById(1L);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("파일 ID로 조회 - 파일 없음")
    void getFileById_notFound() {
        when(fileRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileApplicationService.getFileById(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("파일 검색")
    void searchFiles() {
        List<File> files = Arrays.asList(testFile);
        when(fileRepository.findByKeyword("test")).thenReturn(files);
        try {
            when(fileStorageStrategy.getDownloadUrl(anyString())).thenReturn("http://example.com/file");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<FileResponse> responses = fileApplicationService.searchFiles("test");

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("파일 설명 업데이트")
    void updateFileDescription() {
        FileUpdateCommand command = FileUpdateCommand.builder()
                .id(1L)
                .description("updated description")
                .build();

        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(fileRepository.save(any(File.class))).thenReturn(testFile);
        try {
            when(fileStorageStrategy.getDownloadUrl(anyString())).thenReturn("http://example.com/file");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FileResponse response = fileApplicationService.updateFileDescription(command);

        assertThat(response).isNotNull();
        verify(fileRepository).save(testFile);
    }

    @Test
    @DisplayName("파일 삭제")
    void deleteFile() throws Exception {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        doNothing().when(fileStorageStrategy).deleteFile(anyString());
        doNothing().when(fileRepository).delete(any(File.class));

        fileApplicationService.deleteFile(1L);

        verify(fileStorageStrategy).deleteFile(anyString());
        verify(fileRepository).delete(testFile);
    }

    @Test
    @DisplayName("파일 다운로드")
    void downloadFile() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("test".getBytes()));
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(fileStorageStrategy.downloadFile(anyString())).thenReturn(resource);

        InputStreamResource result = fileApplicationService.downloadFile(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("파일 다운로드 - 예외 발생")
    void downloadFile_exception() throws Exception {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(testFile));
        when(fileStorageStrategy.downloadFile(anyString())).thenThrow(new RuntimeException("download error"));

        assertThatThrownBy(() -> fileApplicationService.downloadFile(1L))
                .isInstanceOf(RuntimeException.class);
    }
}
