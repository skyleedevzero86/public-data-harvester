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
@ConfigurationProperties(prefix = "corp-info")
@Getter
@Setter
@ToString
@Slf4j
public class CorpApiProperties {
    private String url;
    private String endpoint;
    private String serviceKey;
    private Map<String, String> queryParams;

    public URI buildRequestUrlWithBizNo(String bizNo) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                    .path(endpoint);

            queryParams.forEach(builder::queryParam);

            builder.queryParam("brno", bizNo.replaceAll("-", ""));

            String uriWithParam = builder.build().encode().toUriString();

            String finalUri = uriWithParam + "&serviceKey=" + serviceKey;

            return URI.create(finalUri);

        } catch (Exception e) {
            log.error("URI 생성 중 예외 발생: {}", e.getMessage());
            throw e;
        }
    }
}
