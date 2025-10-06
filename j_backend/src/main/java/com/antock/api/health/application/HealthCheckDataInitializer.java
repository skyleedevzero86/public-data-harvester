package com.antock.api.health.application;

import com.antock.api.health.application.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HealthCheckDataInitializer implements CommandLineRunner {

    private final HealthCheckService healthCheckService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Health Check Data Initializer ===");

        try {
            List<String> components = Arrays.asList("database", "redis", "cache", "member-service", "rate-limit");

            log.info("Performing initial health checks for components: {}", components);

            var response = healthCheckService.performSystemHealthCheck(components, "initial");

            log.info("Initial health check completed!");
            log.info("Overall Status: {}", response.getOverallStatus());
            log.info("Healthy Components: {}/{}", response.getHealthyComponents(), response.getTotalComponents());
            log.info("Health Percentage: {}%", response.getHealthPercentage());

            if (response.getComponents() != null) {
                log.info("Component Details:");
                response.getComponents().forEach(component -> {
                    log.info("- {}: {} ({})",
                            component.getComponent(),
                            component.getStatus(),
                            component.getMessage());
                });
            }

        } catch (Exception e) {
            log.error("Error performing initial health check: {}", e.getMessage(), e);
        }
    }
}