package com.antock.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class SlowQueryConfig {

    @Bean
    @Profile("dev")
    public SlowQueryInterceptor slowQueryInterceptor() {
        log.info("SlowQueryInterceptor enabled for development profile");
        return new SlowQueryInterceptor();
    }
}