package com.antock.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"dev", "local", "test"})
public class DataInitConfig {

    private final MemberDataGenerator memberDataGenerator;
    private final DataInitProperties dataInitProperties;

    @Bean
    public CommandLineRunner initMemberData() {
        return args -> {
            if (!dataInitProperties.isEnabled()) {
                log.info("데이터 초기화가 비활성화되어 있습니다. (app.data.init.enabled=false)");
                return;
            }

            log.info("Antock Public Data Harvester 애플리케이션 시작!");
            log.info("더미 데이터 생성 프로세스를 시작합니다...");
            log.info("설정: 회원 {}명, 배치크기 {}, 강제초기화 {}",
                    dataInitProperties.getMemberCount(),
                    dataInitProperties.getBatchSize(),
                    dataInitProperties.isForceInit());

            memberDataGenerator.generateMembers(dataInitProperties.getMemberCount());

            log.info("더미 데이터 생성이 완료되었습니다!");
            log.info("Swagger UI: http://localhost:8080/swagger-ui/index.html");
            log.info("테스트 계정:");
            log.info("   - 관리자: admin0001 / Admin@123!");
            log.info("   - 매니저: manager0001 / Manager@123!");
            log.info("   - 사용자: user0001 / User@123!");
        };
    }
}