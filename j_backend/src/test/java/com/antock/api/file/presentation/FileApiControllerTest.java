package com.antock.api.file.presentation;

import com.antock.api.file.application.dto.FileResponse;
import com.antock.api.file.application.dto.FileUpdateCommand;
import com.antock.api.file.application.service.FileApplicationService;
import com.antock.api.file.application.service.FileValidationService;
import com.antock.global.security.dto.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileApiController.class)
@DisplayName("FileApiController 테스트")
class FileApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileApplicationService fileApplicationService;

    @MockBean
    private FileValidationService fileValidationService;

    @Test
    @DisplayName("파일 업로드 성공")
    @WithMockUser
    void uploadFile_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes());

        FileResponse response = FileResponse.builder()
                .id(1L)
                .originalFileName("test.txt")
                .fileSize(11L)
                .contentType("text/plain")
                .uploadTime(LocalDateTime.now())
                .build();

        doNothing().when(fileValidationService).validateUploadFile(any());
        given(fileApplicationService.uploadFile(any())).willReturn(response);

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .param("description", "테스트 파일")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.originalFileName").value("test.txt"));
    }

    @Test
    @DisplayName("파일 정보 조회 성공")
    @WithMockUser
    void getFileInfo_Success() throws Exception {
        FileResponse response = FileResponse.builder()
                .id(1L)
                .originalFileName("test.txt")
                .fileSize(11L)
                .contentType("text/plain")
                .build();

        given(fileApplicationService.getFileById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/files/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.originalFileName").value("test.txt"));
    }

    @Test
    @DisplayName("파일 목록 조회 성공")
    @WithMockUser
    void getFileList_Success() throws Exception {
        FileResponse file1 = FileResponse.builder()
                .id(1L)
                .originalFileName("file1.txt")
                .build();
        FileResponse file2 = FileResponse.builder()
                .id(2L)
                .originalFileName("file2.txt")
                .build();

        List<FileResponse> files = Arrays.asList(file1, file2);
        given(fileApplicationService.getAllFiles()).willReturn(files);

        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("파일 설명 수정 성공")
    @WithMockUser
    void updateFile_Success() throws Exception {
        FileUpdateCommand command = FileUpdateCommand.builder()
                .description("수정된 설명")
                .build();

        FileResponse response = FileResponse.builder()
                .id(1L)
                .originalFileName("test.txt")
                .description("수정된 설명")
                .build();

        given(fileApplicationService.updateFileDescription(any())).willReturn(response);

        mockMvc.perform(put("/api/v1/files/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("수정된 설명"));
    }

    @Test
    @DisplayName("파일 삭제 성공")
    @WithMockUser
    void deleteFile_Success() throws Exception {
        doNothing().when(fileApplicationService).deleteFile(1L);

        mockMvc.perform(delete("/api/v1/files/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("파일 다운로드 성공")
    @WithMockUser
    void downloadFile_Success() throws Exception {
        InputStreamResource resource = new InputStreamResource(
                new java.io.ByteArrayInputStream("test content".getBytes()));

        FileResponse fileInfo = FileResponse.builder()
                .id(1L)
                .originalFileName("test.txt")
                .fileSize(11L)
                .contentType("text/plain")
                .build();

        given(fileApplicationService.downloadFile(1L)).willReturn(resource);
        given(fileApplicationService.getFileById(1L)).willReturn(fileInfo);

        mockMvc.perform(get("/api/v1/files/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(header().string("Content-Type", "text/plain"));
    }

    @Test
    @DisplayName("파일 검색 성공")
    @WithMockUser
    void searchFiles_Success() throws Exception {
        FileResponse file = FileResponse.builder()
                .id(1L)
                .originalFileName("test.txt")
                .build();

        List<FileResponse> files = Arrays.asList(file);
        given(fileApplicationService.searchFiles(anyString())).willReturn(files);

        mockMvc.perform(get("/api/v1/files/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}

