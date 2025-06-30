package com.antock.api.coseller.application;

import com.antock.api.coseller.application.dto.api.CorpApiJsonResponse;
import com.antock.api.coseller.application.dto.api.CorpItem;
import com.antock.api.coseller.application.dto.properties.CorpApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 법인등록번호 조회 API Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorpApiService {

    private final RestTemplate restTemplate;
    private final CorpApiProperties corpProp;

    @Async
    public CompletableFuture<String> getCorpRegNo(String bizNo){
        //url + param 생성
        URI requestUrl = corpProp.buildRequestUrlWithBizNo(bizNo);
        log.debug("request CorpRegNo API : {} " , requestUrl);
        String corpRegNo = "";

        try{
            ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);

            System.out.println(response.getBody());
            log.debug("CorpRegNo API Response {} : {}", bizNo, response.getBody());

            corpRegNo = parseCorpResponse(response.getBody());

        } catch (ResourceAccessException e) {
            log.error("네트워크 오류 또는 타임아웃: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("RestTemplate 예외 발생: {}", e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 예외: {}", e.getMessage());
        }

        return CompletableFuture.completedFuture(corpRegNo);
    }

    private String parseCorpResponse(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CorpApiJsonResponse response = objectMapper.readValue(json, CorpApiJsonResponse.class);


            CorpItem item = response.getItems().get(0);
            String crno = item.getCrno();
            log.debug("crno = {}", crno);
            return crno;

        } catch (Exception e) {
            log.error("JSON parsing failed: {}", e.getMessage());
        }
        return null;
    }
}
