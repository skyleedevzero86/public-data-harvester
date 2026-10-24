package com.antock.api.coseller.application.service.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LocalFileReadStrategy 경로 검사")
class LocalFileReadStrategyTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("상위 경로 파일명은 거절한다")
    void resolveSafely_rejectsParentPath() {
        LocalFileReadStrategy strategy = strategy();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> strategy.resolveSafely("../application.yml"));

        assertEquals("허용되지 않은 파일 경로입니다.", exception.getMessage());
        assertFalse(exception.getMessage().contains(tempDir.toString()));
    }

    @Test
    @DisplayName("지역 CSV 파일명은 기준 폴더 안으로 해석한다")
    void resolveSafely_allowsRegionCsvName() {
        LocalFileReadStrategy strategy = strategy();

        Path resolved = strategy.resolveSafely("서울_강남구.csv");

        assertTrue(resolved.startsWith(tempDir.toAbsolutePath().normalize()));
        assertEquals("서울_강남구.csv", resolved.getFileName().toString());
    }

    @Test
    @DisplayName("모듈 폴더가 비어 있으면 상위 uploaded-files를 사용한다")
    void resolveWithin_usesParentDirectoryWhenLocalFolderIsEmpty() throws IOException {
        Path moduleDir = tempDir.resolve("j_backend");
        Files.createDirectories(moduleDir.resolve("uploaded-files"));
        Path populated = tempDir.resolve("uploaded-files");
        Files.createDirectories(populated);
        Files.writeString(populated.resolve("서울특별시_강서구.csv"), "header");

        LocalFileReadStrategy strategy = strategy();
        ReflectionTestUtils.setField(strategy, "uploadPath", "uploaded-files");

        Path resolved = strategy.resolveWithin("서울특별시_강서구.csv", moduleDir);

        assertEquals(populated.resolve("서울특별시_강서구.csv").normalize(), resolved.normalize());
    }

    private LocalFileReadStrategy strategy() {
        LocalFileReadStrategy strategy = new LocalFileReadStrategy();
        ReflectionTestUtils.setField(strategy, "uploadPath", tempDir.toString());
        return strategy;
    }
}
