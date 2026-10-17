package com.antock.api.csv.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorpInfoFetcher {

    private final CorpInfoApiClient apiClient;

    @Retryable(retryFor = { ResourceAccessException.class, HttpServerErrorException.class },
            maxAttemptsExpression = "${batch.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${batch.retry.delay:1000}", multiplier = 2))
    public List<Map<String, Object>> fetch(String city, String district) {
        try {
            return apiClient.fetchAll(city, district);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            log.warn("API 호출 실패 (재시도 {}): {} {}, 오류: {}", attemptNumber(), city, district, e.getMessage());
            throw e;
        }
    }

    private int attemptNumber() {
        RetryContext context = RetrySynchronizationManager.getContext();
        if (context == null) {
            return 1;
        }
        return context.getRetryCount() + 1;
    }
}
