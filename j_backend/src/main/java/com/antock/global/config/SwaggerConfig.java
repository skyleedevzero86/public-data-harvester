package com.antock.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.antock.com").description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Antock Public Data Harvester API")
                .description("""
                        공공데이터 수집 및 관리 시스템 API

                        ## 주요 기능
                        - 법인 정보 관리 (CorpMast)
                        - 회원 관리 및 인증
                        - 파일 업로드/다운로드
                        - CSV 배치 처리
                        - 지역별 통계 및 대시보드
                        - 캐시 모니터링 및 관리

                        ## API 그룹
                        - **Admin**: 시스템 관리 및 모니터링
                        - **Corp**: 법인 정보 관리
                        - **Member**: 회원 관리
                        - **File**: 파일 관리
                        - **Dashboard**: 통계 및 대시보드
                        - **CoSeller**: 공동판매자 관리
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Antock Development Team")
                        .email("dev@antock.com")
                        .url("https://antock.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
