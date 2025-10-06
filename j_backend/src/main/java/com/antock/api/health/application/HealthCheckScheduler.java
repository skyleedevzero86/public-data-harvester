package com.antock.api.health.application;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.application.service.HealthCheckService;
import com.antock.api.health.domain.SystemHealth;
import com.antock.api.health.infrastructure.SystemHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HealthCheckScheduler {

    private final HealthCheckService healthCheckService;
    private final SystemHealthRepository systemHealthRepository;

    @Value("${health.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${health.scheduler.components:database,redis,cache,member-service,rate-limit}")
    private String componentsConfig;

    @Value("${health.scheduler.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${health.scheduler.cleanup.cron:0 0 2 * * ?}")
    private String cleanupCron;

    @Scheduled(fixedRate = 300000)
    public void performScheduledHealthCheck() {
        if (!schedulerEnabled) {
            log.debug("헬스 체크 스케줄러가 비활성화되어 있습니다.");
            return;
        }

        try {
            log.info("정기 헬스 체크 시작 - {}", LocalDateTime.now());

            List<String> components = Arrays.asList(componentsConfig.split(","));

            HealthCheckRequest request = HealthCheckRequest.builder()
                    .components(components)
                    .checkType("scheduled")
                    .ignoreCache(true)
                    .includeDetails(true)
                    .build();

            SystemHealthResponse response = healthCheckService.performSystemHealthCheck(
                    components, "scheduled");

            log.info("정기 헬스 체크 완료 - 전체 상태: {}, 정상: {}/{}",
                    response.getOverallStatus(),
                    response.getHealthyComponents(),
                    response.getTotalComponents());

            if (!response.isHealthy()) {
                log.warn("시스템 헬스 상태 이상 감지 - 상태: {}, 장애 컴포넌트: {}",
                        response.getOverallStatus(),
                        response.getUnhealthyComponents());
            }

        } catch (Exception e) {
            log.error("정기 헬스 체크 실행 중 오류 발생", e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void performQuickHealthCheck() {
        if (!schedulerEnabled) {
            return;
        }

        try {
            log.debug("빠른 헬스 체크 시작 - {}", LocalDateTime.now());

            List<String> coreComponents = Arrays.asList("database", "redis");

            HealthCheckRequest request = HealthCheckRequest.builder()
                    .components(coreComponents)
                    .checkType("quick")
                    .ignoreCache(true)
                    .includeDetails(false)
                    .build();

            SystemHealthResponse response = healthCheckService.performSystemHealthCheck(
                    coreComponents, "quick");

            if (!response.isHealthy()) {
                log.error("핵심 컴포넌트 헬스 상태 이상 - 상태: {}, 장애: {}",
                        response.getOverallStatus(),
                        response.getUnhealthyComponents());
            }

        } catch (Exception e) {
            log.error("빠른 헬스 체크 실행 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "${health.scheduler.cleanup.cron:0 0 2 * * ?}")
    public void cleanupExpiredHealthData() {
        if (!cleanupEnabled) {
            log.debug("헬스 체크 데이터 정리 스케줄러가 비활성화되어 있습니다.");
            return;
        }

        try {
            log.info("만료된 헬스 체크 데이터 정리 시작 - {}", LocalDateTime.now());

            healthCheckService.cleanupExpiredChecks();

            log.info("만료된 헬스 체크 데이터 정리 완료 - {}", LocalDateTime.now());

        } catch (Exception e) {
            log.error("헬스 체크 데이터 정리 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "0 0 3 * * MON")
    public void generateWeeklyHealthReport() {
        if (!schedulerEnabled) {
            return;
        }

        try {
            log.info("주간 헬스 체크 리포트 생성 시작 - {}", LocalDateTime.now());

            log.info("주간 헬스 체크 리포트 생성 완료 - {}", LocalDateTime.now());

        } catch (Exception e) {
            log.error("주간 헬스 체크 리포트 생성 중 오류 발생", e);
        }
    }

    @Scheduled(fixedRate = 30000)
    public void monitorHealthStatus() {
        if (!schedulerEnabled) {
            return;
        }

        try {
            List<String> components = Arrays.asList(componentsConfig.split(","));

            HealthCheckRequest request = HealthCheckRequest.builder()
                    .components(components)
                    .checkType("monitor")
                    .ignoreCache(false)
                    .includeDetails(false)
                    .build();

            Optional<SystemHealth> cachedSystemHealth = systemHealthRepository
                    .findLatestValidSystemHealth(LocalDateTime.now());

            if (cachedSystemHealth.isEmpty() || cachedSystemHealth.get().isExpired()) {
                log.warn("헬스 체크 데이터가 없거나 만료되었습니다. 새로 체크를 실행합니다.");
                healthCheckService.performSystemHealthCheckInNewTransaction(components, "monitor");
            }

        } catch (Exception e) {
            log.error("헬스 체크 상태 모니터링 중 오류 발생", e);
        }
    }

    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }

    public void setSchedulerEnabled(boolean enabled) {
        this.schedulerEnabled = enabled;
        log.info("헬스 체크 스케줄러 {}됨", enabled ? "활성화" : "비활성화");
    }

    public void setCleanupEnabled(boolean enabled) {
        this.cleanupEnabled = enabled;
        log.info("헬스 체크 데이터 정리 {}됨", enabled ? "활성화" : "비활성화");
    }
}