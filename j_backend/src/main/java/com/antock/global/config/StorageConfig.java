package com.antock.global.config;

import com.antock.api.file.infrastructure.storage.FileStorageStrategy;
import com.antock.api.file.infrastructure.storage.LocalStorageStrategy;
import com.antock.api.file.infrastructure.storage.MinioStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class StorageConfig {

    @Autowired(required = false)
    private LocalStorageStrategy localStorageStrategy;

    @Autowired(required = false)
    private MinioStorageStrategy minioStorageStrategy;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
    public FileStorageStrategy localFileStorageStrategy() {
        if (localStorageStrategy == null) {
            log.warn("LocalStorageStrategy not available, creating fallback instance");
            return createFallbackLocalStrategy();
        }
        log.info("Local storage strategy selected");
        return localStorageStrategy;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.type", havingValue = "minio")
    public FileStorageStrategy minioFileStorageStrategy() {
        if (minioStorageStrategy == null) {
            throw new IllegalStateException("MinIO storage strategy not available. Check MinIO configuration.");
        }
        log.info("MinIO storage strategy selected");
        return minioStorageStrategy;
    }

    private FileStorageStrategy createFallbackLocalStrategy() {
        return new LocalStorageStrategy() {
            // 필요시 기본 구현 제공
        };
    }
}