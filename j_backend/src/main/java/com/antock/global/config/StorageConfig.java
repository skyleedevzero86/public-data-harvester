package com.antock.global.config;

import com.antock.api.file.infrastructure.storage.FileStorageStrategy;
import com.antock.api.file.infrastructure.storage.LocalStorageStrategy;
import com.antock.api.file.infrastructure.storage.MinioStorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    private final MinioStorageStrategy minioStorageStrategy;
    private final LocalStorageStrategy localStorageStrategy;

    @Bean
    @Primary
    public FileStorageStrategy fileStorageStrategy() {
        if (minioStorageStrategy.isAvailable()) {
            log.info("MinIO 저장소 전략 사용");
            return minioStorageStrategy;
        } else {
            log.info("로컬 저장소 전략 사용");
            return localStorageStrategy;
        }
    }
}