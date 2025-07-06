package com.antock.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "custom.csv")
public class CsvTemplateConfig {
    private String templateDir;
    private String templateName;
}