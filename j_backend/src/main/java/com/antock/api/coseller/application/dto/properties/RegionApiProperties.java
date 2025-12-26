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
@ConfigurationProperties(prefix = "region-info")
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

            queryParams.forEach(builder::queryParam);

            builder.queryParam("keyword", address);

            String uriWithParam = builder.build().encode().toUriString();

            String finalUri = uriWithParam + "&confmKey=" + confmKey;

            return URI.create(finalUri);

        } catch (Exception e) {
            log.error("URI 생성 중 예외 발생: {}", e.getMessage());
            throw e;
        }
    }
}
