package com.antock.api.csv.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.antock.api.csv.config.CsvRetryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SpringJUnitConfig(classes = { CsvRetryConfig.class, CorpInfoFetcherRetryTest.RetryBeans.class })
@TestPropertySource(properties = {
        "batch.retry.max-attempts=3",
        "batch.retry.delay=1"
})
@DisplayName("법인 API 재시도")
class CorpInfoFetcherRetryTest {

    @Autowired
    private CorpInfoFetcher fetcher;

    @Autowired
    private RestTemplate restTemplate;

    @BeforeEach
    void resetRestTemplate() {
        reset(restTemplate);
    }

    @Test
    @DisplayName("연결 오류 2번 뒤 3번째 호출이 성공하면 결과를 반환한다")
    void succeedsOnThirdAttempt() {
        AtomicInteger calls = new AtomicInteger();
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenAnswer(invocation -> {
            int call = calls.incrementAndGet();
            if (call <= 2) {
                throw new ResourceAccessException("연결 실패");
            }
            if (call == 3) {
                return "{\"items\":[{\"bizNo\":\"111\"}]}";
            }
            return "";
        });

        List<Map<String, Object>> result = fetcher.fetch("서울특별시", "강남구");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("bizNo", "111");
        assertThat(calls.get()).isEqualTo(4);
    }

    @Test
    @DisplayName("연결 오류가 3번이면 예외를 던지고 호출은 3회다")
    void throwsAfterThreeFailures() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("연결 실패"));

        assertThatThrownBy(() -> fetcher.fetch("서울특별시", "강남구"))
                .isInstanceOf(ResourceAccessException.class);

        verify(restTemplate, times(3)).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("4xx는 재시도하지 않고 1회만 호출한다")
    void doesNotRetryClientError() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(BAD_REQUEST));

        assertThatThrownBy(() -> fetcher.fetch("서울특별시", "강남구"))
                .isInstanceOf(HttpClientErrorException.class);

        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("XML 응답은 CorpApiResponseException이며 재시도하지 않는다")
    void xmlResponseThrowsWithoutRetry() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("<OpenAPI_ServiceResponse></OpenAPI_ServiceResponse>");

        assertThatThrownBy(() -> fetcher.fetch("서울특별시", "강남구"))
                .isInstanceOf(CorpApiResponseException.class);

        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Configuration
    static class RetryBeans {

        @Bean
        RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }

        @Bean
        CorpInfoApiClient corpInfoApiClient(RestTemplate restTemplate) {
            return new CorpInfoApiClient(restTemplate, "https://example.test", "/corp", "test-key");
        }

        @Bean
        CorpInfoFetcher corpInfoFetcher(CorpInfoApiClient corpInfoApiClient) {
            return new CorpInfoFetcher(corpInfoApiClient);
        }
    }
}
