package com.antock.corp.application.service;

import com.antock.api.coseller.application.service.CorpApiService;
import com.antock.api.coseller.application.dto.properties.CorpApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CorpApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CorpApiProperties corpApiProperties;

    @InjectMocks
    private CorpApiService apiService;

    private String bizNo = ""; //사업자 번호
    private URI mockUri;
    private String mockResponseBody ="";

    @BeforeEach
    void setUp() throws Exception {
        bizNo =  "631-37-01624"; //임의로 추출
        mockUri = new URI("https://test.api");

        mockResponseBody = """
        {
          "items": [
            {
              "crno": "111111-1234567"
            }
          ]
        }
        """;
    }

    @Test
    @DisplayName("API에서 정상 값을 받았을 경우 동작 확인")
    public void get_CorpRegNo_from_API() throws Exception {
        //given
        when(corpApiProperties.buildRequestUrlWithBizNo(bizNo)).thenReturn(mockUri);
        when(restTemplate.getForEntity(mockUri,String.class)).thenReturn(
                new ResponseEntity<>(mockResponseBody, HttpStatus.OK)
        );

        //when
        CompletableFuture<String> future = apiService.getCorpRegNo(bizNo);
        String result = future.get(); //111111-1234567

        //then
        assertThat(result).isEqualTo("111111-1234567");
    }

    @Test
    @DisplayName("API 타임아웃 예외 발생 시 동작 확인")
    void exception_CorpRegNo_from_API() throws Exception {
        //given
        when(corpApiProperties.buildRequestUrlWithBizNo(bizNo)).thenReturn(mockUri);
        when(restTemplate.getForEntity(mockUri,String.class))
                .thenThrow(new ResourceAccessException("Timeout"));

        //when
        String result = apiService.getCorpRegNo(bizNo).get();
        //then
        assertThat(result).isEqualTo("");
    }
}
