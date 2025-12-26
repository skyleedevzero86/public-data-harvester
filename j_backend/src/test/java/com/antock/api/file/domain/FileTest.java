package com.antock.api.file.domain;

import com.antock.api.file.domain.vo.FileContent;
import com.antock.api.file.domain.vo.FileDescription;
import com.antock.api.file.domain.vo.FileMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("File 도메인 테스트")
class FileTest {

    @Test
    @DisplayName("파일 생성")
    void create() {
        File file = File.create("test.txt", "text/plain", 1000L, "test description");

        assertThat(file).isNotNull();
        assertThat(file.getMetadata().getOriginalFileName()).isEqualTo("test.txt");
        assertThat(file.getContent().getFileSize()).isEqualTo(1000L);
        assertThat(file.getDescription().getDescription()).isEqualTo("test description");
    }

    @Test
    @DisplayName("파일 설명 업데이트")
    void updateDescription() {
        File file = File.create("test.txt", "text/plain", 1000L, "old description");
        LocalDateTime beforeUpdate = file.getLastModifiedTime();

        file.updateDescription("new description");

        assertThat(file.getDescription().getDescription()).isEqualTo("new description");
        assertThat(file.getLastModifiedTime()).isAfter(beforeUpdate);
    }
}

