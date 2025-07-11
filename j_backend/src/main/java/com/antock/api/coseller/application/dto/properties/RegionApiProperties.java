package com.antock.api.coseller.application.dto.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
@ConfigurationProperties(prefix="region-info")
@Getter
@Setter
@Slf4j
@ToString
public class RegionApiProperties {
    private String url;
    private String confmKey;
    private Map<String, String> queryParams;

    public URI buildRequestUrlWithAddress(String address) {

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

            // 공통 파라미터 (serviceKey 제외)
            queryParams.forEach(builder::queryParam);

            // 사업자번호
            builder.queryParam("keyword", address);

            // UriComponentsBuilder로 쿼리파라미터까지 만든 후 문자열로 추출
            String uriWithParam = builder.build().encode().toUriString();

            // serviceKey만 이미 인코딩된 값을 그대로 붙이기
            String finalUri = uriWithParam + "&confmKey=" + confmKey;

            // URI 객체 직접 생성
            return URI.create(finalUri);

        } catch (Exception e) {
            log.error("URI 생성 중 예외 발생: {}" , e.getMessage());
            throw e;
        }
    }
}
