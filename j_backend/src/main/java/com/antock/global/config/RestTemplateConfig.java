package com.antock.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            // 요청 로깅
            System.out.println("Request URI: " + request.getURI());
            System.out.println("Request Method: " + request.getMethod());
            System.out.println("Request Headers: " + request.getHeaders());
            System.out.println("Request Body: " + new String(body, StandardCharsets.UTF_8));

            ClientHttpResponse response = execution.execute(request, body);

            // 응답 로깅
            System.out.println("Response Status Code: " + response.getStatusCode());
            System.out.println("Response Headers: " + response.getHeaders());

            return response;
        });

        return restTemplate;
    }
}