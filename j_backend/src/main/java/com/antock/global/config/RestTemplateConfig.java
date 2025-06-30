package com.antock.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // 요청 로깅
            log.debug("Request URI: {} ", request.getURI());
            log.info("Request processing");

            ClientHttpResponse response = execution.execute(request, body);

            // 응답 로깅
            log.debug("Response Status Code: {}" , response.getStatusCode());
            log.info("Response received");

            return response;
        });

        return restTemplate;
    }
}