package com.antock.global.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.data.init")
public class DataInitProperties {
    private boolean enabled = false;
    private int memberCount = 2000;
    private boolean forceInit = false;
    private int batchSize = 500;
}