package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.client.RegionApiClient;
import com.antock.api.coseller.application.dto.api.RegionApiJsonResponse;
import com.antock.api.coseller.application.dto.properties.RegionApiProperties;
import com.antock.global.common.exception.ExternalApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import com.antock.global.utils.AddressUtil;

/**
 * 행정구혁코드 조회 API Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionApiService implements RegionApiClient {

    private final RestTemplate restTemplate;
    private final RegionApiProperties regionProp;

    @Async
    public CompletableFuture<String> getRegionCode(String address) {
        // 시/구/읍,면.동 까지만 추출해서 행정코드 조회 요청 (주소가 온전하지 않음)
        String fomattedAddress = AddressUtil.extractAddress(address);
        //url 생성
        URI requestUrl = regionProp.buildRequestUrlWithAddress(fomattedAddress);
        log.debug("request RegionCd API : {}", requestUrl);

        String regionCd = "";

        try{
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            log.debug("Region API Response {} : {}", address, response.getBody());
            log.info("Region API get Response success");

            if (response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.completedFuture(parseRegionResponse(response.getBody()));
            } else {
                log.error("Region API 응답 실패: status = {}", response.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            log.error("네트워크 오류 또는 타임아웃: {}", e.getMessage());
        } catch (RestClientException e) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "통신에 실패하였습니다.");

        } catch (Exception e) {
            throw new ExternalApiException(HttpStatus.BAD_GATEWAY, "통신에 실패하였습니다.");
        }

        return CompletableFuture.completedFuture(regionCd);
    }

    private String parseRegionResponse(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RegionApiJsonResponse response = objectMapper.readValue(json, RegionApiJsonResponse.class);

            String regionCd = response.getResults().getJuso().get(0).getAdmCd();
            log.debug("regionCd={}", regionCd);
            return regionCd;

        } catch (Exception e) {
            log.error("Region API 응답 파싱 실패: {}", e.getMessage());
        }

        return "";
    }
}
