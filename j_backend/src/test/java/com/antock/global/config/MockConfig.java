package com.antock.global.config;

import com.antock.api.coseller.application.service.CoSellerService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockConfig {

    @Bean
    public CoSellerService coSellerService() {
        return Mockito.mock(CoSellerService.class);
    }

}
