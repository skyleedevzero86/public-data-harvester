package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import com.antock.api.coseller.application.dto.properties.RegionApiProperties;
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
public class RegionApiService implements RegionApiClient {

    private final RestTemplate restTemplate;
    private final RegionApiProperties regionProp;
    private final ObjectMapper objectMapper;

    @Override
    public CompletableFuture<String> getRegionCode(String address) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI requestUrl = regionProp.buildRequestUrlWithAddress(address);
                log.info("지역 API 요청 URL: {}", requestUrl);

                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String regionCode = parseRegionCodeFromResponse(response.getBody());
                    log.info("지역코드 추출 성공: address={}, regionCode={}", address, regionCode);
                    return regionCode;
                } else {
                    log.warn("지역 API 응답 실패: status={}, body={}", response.getStatusCode(), response.getBody());
                    throw new ExternalApiException(
                            HttpStatus.BAD_REQUEST,
                            "지역 API 응답이 올바르지 않습니다: " + response.getStatusCode(),
                            "Region API",
                            requestUrl.toString()
                    );
                }
            } catch (RestClientException e) {
                log.error("지역 API 호출 중 RestClientException 발생: address={}, error={}", address, e.getMessage(), e);
                throw new ExternalApiException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "지역 API 서비스에 연결할 수 없습니다: " + e.getMessage(),
                        "Region API",
                        regionProp.getUrl()
                );
            } catch (Exception e) {
                log.error("지역 API 호출 중 예상치 못한 오류 발생: address={}, error={}", address, e.getMessage(), e);
                throw new ExternalApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "지역 API 처리 중 오류가 발생했습니다: " + e.getMessage(),
                        "Region API",
                        regionProp.getUrl()
                );
            }
        });
    }

    @Override
    public CompletableFuture<RegionInfoDto> getRegionInfo(String address) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI requestUrl = regionProp.buildRequestUrlWithAddress(address);
                log.info("지역 정보 API 요청 URL: {}", requestUrl);

                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    RegionInfoDto regionInfo = parseRegionInfoFromResponse(response.getBody());
                    log.info("지역 정보 추출 성공: address={}, regionInfo={}", address, regionInfo);
                    return regionInfo;
                } else {
                    log.warn("지역 정보 API 응답 실패: status={}, body={}", response.getStatusCode(), response.getBody());
                    throw new ExternalApiException(
                            HttpStatus.BAD_REQUEST,
                            "지역 정보 API 응답이 올바르지 않습니다: " + response.getStatusCode(),
                            "Region Info API",
                            requestUrl.toString()
                    );
                }
            } catch (RestClientException e) {
                log.error("지역 정보 API 호출 중 RestClientException 발생: address={}, error={}", address, e.getMessage(), e);
                throw new ExternalApiException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "지역 정보 API 서비스에 연결할 수 없습니다: " + e.getMessage(),
                        "Region Info API",
                        regionProp.getUrl()
                );
            } catch (Exception e) {
                log.error("지역 정보 API 호출 중 예상치 못한 오류 발생: address={}, error={}", address, e.getMessage(), e);
                throw new ExternalApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "지역 정보 API 처리 중 오류가 발생했습니다: " + e.getMessage(),
                        "Region Info API",
                        regionProp.getUrl()
                );
            }
        });
    }

    private String parseRegionCodeFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode commonNode = rootNode.path("results").path("common");
            if (commonNode.has("errorCode") && !commonNode.get("errorCode").asText().equals("0")) {
                String errorMessage = commonNode.path("errorMessage").asText("알 수 없는 오류");
                throw new RuntimeException("API 오류: " + errorMessage);
            }

            JsonNode jusoArray = rootNode.path("results").path("juso");
            if (jusoArray.isArray() && jusoArray.size() > 0) {
                JsonNode firstJuso = jusoArray.get(0);
                String admCd = firstJuso.path("admCd").asText();

                if (admCd != null && !admCd.trim().isEmpty()) {
                    return admCd;
                } else {
                    throw new RuntimeException("지역코드(admCd)가 응답에 포함되지 않았습니다");
                }
            } else {
                throw new RuntimeException("주소 정보(juso)가 응답에 포함되지 않았습니다");
            }
        } catch (Exception e) {
            log.error("지역코드 파싱 실패: responseBody={}, error={}", responseBody, e.getMessage(), e);
            throw new RuntimeException("지역코드 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private RegionInfoDto parseRegionInfoFromResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode commonNode = rootNode.path("results").path("common");
            if (commonNode.has("errorCode") && !commonNode.get("errorCode").asText().equals("0")) {
                String errorMessage = commonNode.path("errorMessage").asText("알 수 없는 오류");
                throw new RuntimeException("API 오류: " + errorMessage);
            }

            JsonNode jusoArray = rootNode.path("results").path("juso");
            if (jusoArray.isArray() && jusoArray.size() > 0) {
                JsonNode firstJuso = jusoArray.get(0);

                String regionCd = firstJuso.path("admCd").asText();
                String siNm = firstJuso.path("siNm").asText();
                String sggNm = firstJuso.path("sggNm").asText();
                String corpRegNo = firstJuso.path("corpRegNo").asText();
                String rnMgtSn = firstJuso.path("rnMgtSn").asText();

                if (regionCd == null || regionCd.trim().isEmpty()) {
                    throw new RuntimeException("지역코드(admCd)가 응답에 포함되지 않았습니다");
                }

                return RegionInfoDto.builder()
                        .regionCd(regionCd)
                        .siNm(siNm != null ? siNm.trim() : "")
                        .sggNm(sggNm != null ? sggNm.trim() : "")
                        .corpRegNo(corpRegNo != null ? corpRegNo.trim() : "")
                        .rnMgtSn(rnMgtSn != null ? rnMgtSn.trim() : "")
                        .build();
            } else {
                throw new RuntimeException("주소 정보(juso)가 응답에 포함되지 않았습니다");
            }
        } catch (Exception e) {
            log.error("지역 정보 파싱 실패: responseBody={}, error={}", responseBody, e.getMessage(), e);
            throw new RuntimeException("지역 정보 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }
}