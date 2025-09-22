package com.antock.global.config;

import com.antock.api.health.application.HealthCheckScheduler;
import com.antock.api.health.application.service.HealthCheckService;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import com.antock.api.health.infrastructure.SystemHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class HealthConfig {

    private final HealthCheckRepository healthCheckRepository;
    private final SystemHealthRepository systemHealthRepository;
    private final HealthCheckService healthCheckService;
    private final HealthCheckScheduler healthCheckScheduler;

    @Bean
    public HealthIndicator newSystemHealthIndicator() {
        return () -> {
            try {
                var request = com.antock.api.health.application.dto.HealthCheckRequest.builder()
                        .ignoreCache(false)
                        .includeDetails(false)
                        .build();

                var response = healthCheckService.getSystemHealth(request);

                if (response.isHealthy()) {
                    return org.springframework.boot.actuate.health.Health.up()
                            .withDetail("status", response.getOverallStatus().getCode())
                            .withDetail("healthyComponents", response.getHealthyComponents())
                            .withDetail("totalComponents", response.getTotalComponents())
                            .withDetail("healthPercentage", response.getHealthPercentage())
                            .build();
                } else {
                    return org.springframework.boot.actuate.health.Health.down()
                            .withDetail("status", response.getOverallStatus().getCode())
                            .withDetail("unhealthyComponents", response.getUnhealthyComponents())
                            .withDetail("totalComponents", response.getTotalComponents())
                            .withDetail("healthPercentage", response.getHealthPercentage())
                            .build();
                }
            } catch (Exception e) {
                log.error("헬스 인디케이터 실행 중 오류", e);
                return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator newDatabaseHealthIndicator() {
        return () -> {
            try {
                var request = com.antock.api.health.application.dto.HealthCheckRequest.builder()
                        .components(java.util.Arrays.asList("database"))
                        .ignoreCache(false)
                        .includeDetails(false)
                        .build();

                var response = healthCheckService.getSystemHealth(request);

                if (response.isHealthy()) {
                    return org.springframework.boot.actuate.health.Health.up()
                            .withDetail("component", "database")
                            .withDetail("status", "UP")
                            .build();
                } else {
                    return org.springframework.boot.actuate.health.Health.down()
                            .withDetail("component", "database")
                            .withDetail("status", "DOWN")
                            .build();
                }
            } catch (Exception e) {
                log.error("데이터베이스 헬스 인디케이터 실행 중 오류", e);
                return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("component", "database")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator newRedisHealthIndicator() {
        return () -> {
            try {
                var request = com.antock.api.health.application.dto.HealthCheckRequest.builder()
                        .components(java.util.Arrays.asList("redis"))
                        .ignoreCache(false)
                        .includeDetails(false)
                        .build();

                var response = healthCheckService.getSystemHealth(request);

                if (response.isHealthy()) {
                    return org.springframework.boot.actuate.health.Health.up()
                            .withDetail("component", "redis")
                            .withDetail("status", "UP")
                            .build();
                } else {
                    return org.springframework.boot.actuate.health.Health.down()
                            .withDetail("component", "redis")
                            .withDetail("status", "DOWN")
                            .build();
                }
            } catch (Exception e) {
                log.error("Redis 헬스 인디케이터 실행 중 오류", e);
                return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("component", "redis")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
}