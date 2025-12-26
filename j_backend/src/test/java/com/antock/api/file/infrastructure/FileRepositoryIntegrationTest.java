package com.antock.api.file.infrastructure;

import com.antock.api.file.domain.File;
import com.antock.api.file.domain.vo.FileContent;
import com.antock.api.file.domain.vo.FileDescription;
import com.antock.api.file.domain.vo.FileMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FileRepository 통합 테스트")
class FileRepositoryIntegrationTest {

    @Autowired
    private FileRepository fileRepository;

    @Test
    @DisplayName("파일 저장 및 조회")
    void saveAndFind() {
        File file = File.builder()
                .metadata(FileMetadata.of("test.txt", "uuid_test.txt", "text/plain"))
                .content(FileContent.of(1000L))
                .description(FileDescription.of("test description"))
                .uploadTime(LocalDateTime.now())
                .lastModifiedTime(LocalDateTime.now())
                .build();

        File saved = fileRepository.save(file);
        File found = fileRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getMetadata().getOriginalFileName()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("키워드로 파일 검색")
    void findByKeyword() {
        File file1 = File.builder()
                .metadata(FileMetadata.of("test1.txt", "uuid1", "text/plain"))
                .content(FileContent.of(100L))
                .description(FileDescription.of("test file"))
                .uploadTime(LocalDateTime.now())
                .lastModifiedTime(LocalDateTime.now())
                .build();

        File file2 = File.builder()
                .metadata(FileMetadata.of("document.pdf", "uuid2", "application/pdf"))
                .content(FileContent.of(200L))
                .description(FileDescription.of("document"))
                .uploadTime(LocalDateTime.now())
                .lastModifiedTime(LocalDateTime.now())
                .build();

        fileRepository.save(file1);
        fileRepository.save(file2);

        List<File> results = fileRepository.findByKeyword("test");

        assertThat(results).isNotEmpty();
    }
}

