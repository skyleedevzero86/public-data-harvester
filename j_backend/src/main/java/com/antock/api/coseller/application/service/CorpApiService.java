package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.CorpApiClient;
import com.antock.api.coseller.application.dto.properties.CorpApiProperties;
import com.antock.global.common.exception.ExternalApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorpApiService implements CorpApiClient {

    private final RestTemplate restTemplate;
    private final CorpApiProperties corpProp;
    private final ObjectMapper objectMapper;

    @Override
    public CompletableFuture<String> getCorpRegNo(String bizNo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI requestUrl = corpProp.buildRequestUrlWithBizNo(bizNo);
                log.info("법인 API 요청 URL: {}", requestUrl);

                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String corpRegNo = parseCorpRegNoFromResponse(response.getBody());
                    log.info("법인등록번호 추출 성공: bizNo={}, corpRegNo={}", bizNo, corpRegNo);
                    return corpRegNo;
                } else {
                    log.warn("법인 API 응답 실패: status={}, body={}", response.getStatusCode(), response.getBody());
                    throw new ExternalApiException(
                            HttpStatus.BAD_REQUEST,
                            "법인 API 응답이 올바르지 않습니다: " + response.getStatusCode(),
                            "Corp API",
                            requestUrl.toString()
                    );
                }
            } catch (RestClientException e) {
                log.error("법인 API 호출 중 RestClientException 발생: bizNo={}, error={}", bizNo, e.getMessage(), e);
                throw new ExternalApiException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "법인 API 서비스에 연결할 수 없습니다: " + e.getMessage(),
                        "Corp API",
                        corpProp.getUrl()
                );
            } catch (Exception e) {
                log.error("법인 API 호출 중 예상치 못한 오류 발생: bizNo={}, error={}", bizNo, e.getMessage(), e);
                throw new ExternalApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "법인 API 처리 중 오류가 발생했습니다: " + e.getMessage(),
                        "Corp API",
                        corpProp.getUrl()
                );
            }
        });
    }

    private String parseCorpRegNoFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode commonNode = rootNode.path("results").path("common");
            if (commonNode.has("errorCode") && !commonNode.get("errorCode").asText().equals("0")) {
                String errorMessage = commonNode.path("errorMessage").asText("알 수 없는 오류");
                throw new RuntimeException("API 오류: " + errorMessage);
            }

            JsonNode corpArray = rootNode.path("results").path("corp");
            if (corpArray.isArray() && corpArray.size() > 0) {
                JsonNode firstCorp = corpArray.get(0);
                String corpRegNo = firstCorp.path("corpRegNo").asText();

                if (corpRegNo != null && !corpRegNo.trim().isEmpty()) {
                    return corpRegNo;
                } else {
                    throw new RuntimeException("법인등록번호(corpRegNo)가 응답에 포함되지 않았습니다");
                }
            } else {
                throw new RuntimeException("법인 정보(corp)가 응답에 포함되지 않았습니다");
            }
        } catch (Exception e) {
            log.error("법인등록번호 파싱 실패: responseBody={}, error={}", responseBody, e.getMessage(), e);
            throw new RuntimeException("법인등록번호 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }
}