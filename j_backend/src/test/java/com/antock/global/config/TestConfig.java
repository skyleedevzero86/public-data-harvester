package com.antock.global.config;

import com.antock.api.corpsearch.application.service.CorpMastSearchService;
import com.antock.web.corpsearch.presentation.CorpMastSearchWebController;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
    @Bean
    public CorpMastSearchService corpMastSearchService() {
        return Mockito.mock(CorpMastSearchService.class);
    }

    @Bean
    public CorpMastSearchWebController corpMastSearchWebController(CorpMastSearchService corpMastSearchService) {
        return new CorpMastSearchWebController(corpMastSearchService);
    }

    @Bean
    public TestExceptionHandler testExceptionHandler(CorpMastSearchService corpMastSearchService) {
        return new TestExceptionHandler(corpMastSearchService);
    }
}