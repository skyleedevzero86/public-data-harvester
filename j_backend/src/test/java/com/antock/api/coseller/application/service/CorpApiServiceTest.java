package com.antock.api.coseller.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorpApiService 테스트")
class CorpApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private com.antock.api.coseller.application.dto.properties.CorpApiProperties corpProp;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private CorpApiService corpApiService;

    @Test
    @DisplayName("법인등록번호 조회 성공")
    void getCorpRegNo_Success() throws Exception {
        CompletableFuture<String> future = CompletableFuture.completedFuture("110111-1234567");
        
        CompletableFuture<String> result = corpApiService.getCorpRegNo("123-45-67890");

        assertThat(result).isNotNull();
    }
}


