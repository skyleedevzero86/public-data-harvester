package com.antock.corp.application.service;

import com.antock.api.coseller.application.dto.properties.CorpApiProperties;
import com.antock.api.coseller.application.service.CorpApiService;
import com.antock.global.common.exception.ExternalApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CorpApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CorpApiProperties corpApiProperties;

    private CorpApiService apiService;

    private String bizNo = "";
    private URI mockUri;
    private String mockResponseBody = "";

    @BeforeEach
    void setUp() throws Exception {
        apiService = new CorpApiService(restTemplate, corpApiProperties, new ObjectMapper());
        bizNo = "631-37-01624";
        mockUri = new URI("https://test.api");

        mockResponseBody = """
                {
                  "results": {
                    "common": { "errorCode": "0" },
                    "corp": [ { "corpRegNo": "111111-1234567" } ]
                  }
                }
                """;
    }

    @Test
    @DisplayName("API에서 정상 값을 받았을 경우 동작 확인")
    public void get_CorpRegNo_from_API() throws Exception {
        when(corpApiProperties.buildRequestUrlWithBizNo(bizNo)).thenReturn(mockUri);
        when(restTemplate.getForEntity(mockUri, String.class)).thenReturn(
                new ResponseEntity<>(mockResponseBody, HttpStatus.OK));

        String result = apiService.getCorpRegNo(bizNo).get();

        assertThat(result).isEqualTo("111111-1234567");
    }

    @Test
    @DisplayName("API 타임아웃 예외 발생 시 동작 확인")
    void exception_CorpRegNo_from_API() {
        when(corpApiProperties.buildRequestUrlWithBizNo(bizNo)).thenReturn(mockUri);
        when(corpApiProperties.getUrl()).thenReturn("https://test.api");
        when(restTemplate.getForEntity(mockUri, String.class))
                .thenThrow(new ResourceAccessException("Timeout"));

        assertThatThrownBy(() -> apiService.getCorpRegNo(bizNo).get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ExternalApiException.class);
    }
}
