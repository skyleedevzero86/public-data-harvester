package com.antock.api.csv.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Component
public class CorpInfoApiClient {
    private final RestTemplate restTemplate;
    private final String url;
    private final String endpoint;
    private final String serviceKey;

    public CorpInfoApiClient(
            RestTemplate restTemplate,
            @Value("${corp-info.url:https://apis.data.go.kr/1130000/MllBsDtl_2Service}") String url,
            @Value("${corp-info.endpoint:/getMllBsInfoDetail_2}") String endpoint,
            @Value("${corp-info.serviceKey:gm4VRA9rbbd%2BQ6%2F%2FknBcl3lZ5GIm0fq37PbyHPrRJdJ9xjsQlHOsqcb2j0M1coSz2AWOduxHVBLZwj8pSsm88A%3D%3D}") String serviceKey) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.endpoint = endpoint;
        this.serviceKey = serviceKey;
    }

    public List<Map<String, Object>> fetchAll(String city, String district) {
        List<Map<String, Object>> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (int page = 1; page <= 49; page++) {
            String apiUrl = String.format(
                    "%s%s?serviceKey=%s&pageNo=%d&numOfRows=1000&resultType=json&ctpvNm=%s&signguNm=%s",
                    url, endpoint, serviceKey, page, encode(city), encode(district));
            log.info("API 호출 URL: {}", maskServiceKey(apiUrl));
            String responseStr = restTemplate.getForObject(apiUrl, String.class);
            if (responseStr == null || responseStr.trim().isEmpty())
                break;
            if (responseStr.trim().startsWith("<")) {
                log.error("API 응답이 XML입니다. JSON 객체를 기대했습니다.");
                throw new CorpApiResponseException("법인 API 응답이 XML입니다. JSON 객체를 기대했습니다.");
            }
            Map<String, Object> response = readResponse(mapper, responseStr);
            Object itemsObj = response.get("items");
            if (itemsObj instanceof List<?> items) {
                if (items.isEmpty())
                    break;
                for (Object item : items) {
                    if (item instanceof Map) {
                        result.add((Map<String, Object>) item);
                    }
                }
            } else {
                throw new CorpApiResponseException("법인 API 응답의 items가 배열이 아닙니다. JSON 배열을 기대했습니다.");
            }
        }
        return result;
    }

    private Map<String, Object> readResponse(ObjectMapper mapper, String responseStr) {
        try {
            return mapper.readValue(responseStr, Map.class);
        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            throw new CorpApiResponseException("법인 API 응답 JSON 파싱에 실패했습니다. JSON 객체를 기대했습니다.", e);
        }
    }

    private String maskServiceKey(String apiUrl) {
        if (serviceKey == null || serviceKey.isEmpty()) {
            return apiUrl;
        }
        return apiUrl.replace(serviceKey, "****");
    }

    private static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }
}