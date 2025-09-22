package com.antock.web.health.presentation;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.application.dto.HealthMetricsResponse;
import com.antock.api.health.application.service.HealthCheckService;
import com.antock.api.health.application.service.HealthMetricsService;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthWebController {

    private final HealthCheckService healthCheckService;
    private final HealthMetricsService healthMetricsService;

    @GetMapping
    public String healthDashboard(Model model, @CurrentUser AuthenticatedUser user) {
        try {
            HealthCheckRequest request = HealthCheckRequest.builder()
                    .ignoreCache(false)
                    .includeDetails(true)
                    .build();

            SystemHealthResponse systemHealth = healthCheckService.getSystemHealth(request);
            model.addAttribute("systemHealth", systemHealth);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("checkedAt").descending());
            Page<HealthCheckResponse> recentChecks = healthCheckService.getHealthHistory(
                    LocalDateTime.now().minusHours(24), pageable);
            model.addAttribute("recentChecks", recentChecks.getContent());

            List<String> components = Arrays.asList("database", "redis", "cache", "member-service", "rate-limit");
            model.addAttribute("components", components);

            return "health/dashboard";
        } catch (Exception e) {
            log.error("헬스 대시보드 조회 실패", e);
            model.addAttribute("error", "헬스 상태를 조회할 수 없습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/component/{component}")
    public String componentHealth(@PathVariable String component,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  Model model, @CurrentUser AuthenticatedUser user) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("checkedAt").descending());
            Page<HealthCheckResponse> healthChecks = healthCheckService.getComponentHealth(component, pageable);

            model.addAttribute("component", component);
            model.addAttribute("healthChecks", healthChecks);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", healthChecks.getTotalPages());
            model.addAttribute("totalElements", healthChecks.getTotalElements());

            return "health/component";
        } catch (Exception e) {
            log.error("컴포넌트 헬스 상태 조회 실패: {}", component, e);
            model.addAttribute("error", "컴포넌트 헬스 상태를 조회할 수 없습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/history")
    public String healthHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model, @CurrentUser AuthenticatedUser user) {
        try {
            LocalDateTime startDate = fromDate != null ? fromDate : LocalDateTime.now().minusDays(7);
            Pageable pageable = PageRequest.of(page, size, Sort.by("checkedAt").descending());
            Page<HealthCheckResponse> healthChecks = healthCheckService.getHealthHistory(startDate, pageable);

            model.addAttribute("healthChecks", healthChecks);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", healthChecks.getTotalPages());
            model.addAttribute("totalElements", healthChecks.getTotalElements());

            return "health/history";
        } catch (Exception e) {
            log.error("헬스 체크 이력 조회 실패", e);
            model.addAttribute("error", "헬스 체크 이력을 조회할 수 없습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @PostMapping("/check")
    public String performHealthCheck(@RequestParam(required = false) List<String> components,
                                     Model model, @CurrentUser AuthenticatedUser user) {
        try {
            if (components == null || components.isEmpty()) {
                components = Arrays.asList("database", "redis", "cache", "member-service", "rate-limit");
            }

            SystemHealthResponse response = healthCheckService.performSystemHealthCheck(components, "manual");
            model.addAttribute("systemHealth", response);
            model.addAttribute("message", "헬스 체크가 완료되었습니다.");

            return "health/result";
        } catch (Exception e) {
            log.error("수동 헬스 체크 실행 실패", e);
            model.addAttribute("error", "헬스 체크 실행 중 오류가 발생했습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/status")
    public String healthStatus(Model model) {
        try {
            HealthCheckRequest request = HealthCheckRequest.builder()
                    .ignoreCache(false)
                    .includeDetails(false)
                    .build();

            SystemHealthResponse response = healthCheckService.getSystemHealth(request);
            model.addAttribute("systemHealth", response);

            return "health/status";
        } catch (Exception e) {
            log.error("헬스 상태 조회 실패", e);
            model.addAttribute("error", "헬스 상태를 조회할 수 없습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @PostMapping("/cleanup")
    public String cleanupExpiredData(Model model, @CurrentUser AuthenticatedUser user) {
        try {
            healthCheckService.cleanupExpiredChecks();
            model.addAttribute("message", "만료된 헬스 체크 데이터가 정리되었습니다.");

            return "health/result";
        } catch (Exception e) {
            log.error("헬스 체크 데이터 정리 실패", e);
            model.addAttribute("error", "데이터 정리 중 오류가 발생했습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/metrics")
    public String healthMetrics(@RequestParam(defaultValue = "7") int days,
                                Model model, @CurrentUser AuthenticatedUser user) {
        try {
            HealthMetricsResponse metrics = healthMetricsService.calculateSystemMetrics(days);
            model.addAttribute("metrics", metrics);
            model.addAttribute("days", days);

            return "health/metrics";
        } catch (Exception e) {
            log.error("헬스 체크 메트릭 조회 실패", e);
            model.addAttribute("error", "메트릭 조회 중 오류가 발생했습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/metrics/component/{component}")
    public String componentMetrics(@PathVariable String component,
                                   @RequestParam(defaultValue = "7") int days,
                                   Model model, @CurrentUser AuthenticatedUser user) {
        try {
            HealthMetricsResponse.ComponentMetrics metrics = healthMetricsService.calculateComponentMetrics(component,
                    days);
            model.addAttribute("metrics", metrics);
            model.addAttribute("component", component);
            model.addAttribute("days", days);

            return "health/component-metrics";
        } catch (Exception e) {
            log.error("컴포넌트 메트릭 조회 실패: {}", component, e);
            model.addAttribute("error", "컴포넌트 메트릭 조회 중 오류가 발생했습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/component-metrics")
    public String componentMetricsDefault(
            @RequestParam(required = false) String component,
            @RequestParam(defaultValue = "7") int days,
            Model model, @CurrentUser AuthenticatedUser user) {
        try {
            if (component == null || component.isEmpty()) {
                component = "database";
            }

            HealthMetricsResponse.ComponentMetrics metrics = healthMetricsService.calculateComponentMetrics(component,
                    days);
            model.addAttribute("metrics", metrics);
            model.addAttribute("component", component);
            model.addAttribute("days", days);

            return "health/component-metrics";
        } catch (Exception e) {
            log.error("컴포넌트 메트릭 조회 실패: {}", component, e);
            model.addAttribute("error", "컴포넌트 메트릭 조회 중 오류가 발생했습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/metrics/realtime")
    public String realtimeMetrics(Model model, @CurrentUser AuthenticatedUser user) {
        try {
            HealthMetricsResponse metrics = healthMetricsService.calculateRealtimeMetrics();
            model.addAttribute("metrics", metrics);

            return "health/realtime-metrics";
        } catch (Exception e) {
            log.error("실시간 메트릭 조회 실패", e);
            model.addAttribute("error", "실시간 메트릭 조회 중 오류가 발생했습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/realtime-metrics")
    public String realtimeMetricsRedirect() {
        return "redirect:/health/metrics/realtime";
    }

    @GetMapping("/error")
    public String healthError(
            @RequestParam(required = false) String error,
            Model model) {
        log.warn("헬스 체크 오류 페이지 접근: {}", error);

        if (error != null) {
            model.addAttribute("error", error);
        } else {
            model.addAttribute("error", "알 수 없는 오류가 발생했습니다.");
        }

        return "health/error";
    }

    @GetMapping("/result")
    public String healthResult(
            @RequestParam(required = false) String message,
            Model model) {
        log.info("헬스 체크 결과 페이지 접근: {}", message);

        if (message != null) {
            model.addAttribute("message", message);
        } else {
            model.addAttribute("message", "헬스 체크가 완료되었습니다.");
        }

        return "health/result";
    }
}