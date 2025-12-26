package com.antock.api.file.application.service;

import com.antock.api.file.application.dto.FileUpdateCommand;
import com.antock.api.file.application.dto.FileUploadCommand;
import com.antock.api.file.domain.File;
import com.antock.api.file.infrastructure.FileRepository;
import com.antock.api.member.domain.Member;
import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("File 서비스 통합 테스트")
class FileServiceIntegrationTest {

    @Autowired
    private FileApplicationService fileApplicationService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.findByUsername("testuser")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .username("testuser")
                            .password("encoded")
                            .email("test@test.com")
                            .nickname("test")
                            .status(MemberStatus.APPROVED)
                            .role(Role.USER)
                            .build();
                    return memberRepository.save(member);
                });
    }

    @Test
    @DisplayName("파일 업로드 및 조회 통합 테스트")
    void uploadAndGetFile_integration() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes());

        FileUploadCommand uploadCommand = FileUploadCommand.builder()
                .file(multipartFile)
                .description("test file")
                .uploaderId(testMember.getId())
                .uploaderName(testMember.getUsername())
                .build();

        var uploadResponse = fileApplicationService.uploadFile(uploadCommand);
        assertThat(uploadResponse).isNotNull();

        var fileResponse = fileApplicationService.getFileById(uploadResponse.getId());
        assertThat(fileResponse).isNotNull();
        assertThat(fileResponse.getOriginalFileName()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("파일 설명 업데이트 통합 테스트")
    void updateFileDescription_integration() {
        File file = File.builder()
                .metadata(com.antock.api.file.domain.vo.FileMetadata.of("test.txt", "stored.txt", "text/plain"))
                .content(com.antock.api.file.domain.vo.FileContent.of(100L))
                .description(com.antock.api.file.domain.vo.FileDescription.of("original"))
                .uploadTime(java.time.LocalDateTime.now())
                .lastModifiedTime(java.time.LocalDateTime.now())
                .build();
        file = fileRepository.save(file);

        FileUpdateCommand updateCommand = FileUpdateCommand.builder()
                .id(file.getId())
                .description("updated description")
                .build();

        var updatedResponse = fileApplicationService.updateFileDescription(updateCommand);
        assertThat(updatedResponse).isNotNull();
    }
}
