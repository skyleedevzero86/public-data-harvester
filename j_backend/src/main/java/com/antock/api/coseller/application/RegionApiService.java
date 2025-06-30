package com.antock.api.coseller.application;

import com.antock.api.coseller.application.dto.api.RegionApiJsonResponse;
import com.antock.api.coseller.application.dto.properties.RegionApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * 행정구혁코드 조회 API Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegionApiService {

    private final RestTemplate restTemplate;
    private final RegionApiProperties regionProp;

    @Async
    public CompletableFuture<String> getRegionCode(String address) {
        //url 생성
        URI requestUrl = regionProp.buildRequestUrlWithAddress(address);
        log.debug("request RegionCd API : {}", requestUrl);
        System.out.println("request::::"+requestUrl);
        String regionCd = "";

        try{
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
            log.debug("CorpRegNo API Response {} : {}", address, response.getBody());
            log.info("CorpRegNo API get Response success");
            System.out.println("response::"+response.getBody());
            if (response.getStatusCode().is2xxSuccessful()) {
                return CompletableFuture.completedFuture(parseRegionResponse(response.getBody()));
            } else {
                log.error("Region API 응답 실패: status = {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Region API 호출 실패: {}", e.getMessage());
        }

        return CompletableFuture.completedFuture("region ");
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

        return null;
    }
}
