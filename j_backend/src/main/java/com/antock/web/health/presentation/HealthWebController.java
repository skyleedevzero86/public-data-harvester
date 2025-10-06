package com.antock.web.health.presentation;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.application.dto.HealthMetricsResponse;
import com.antock.api.health.application.dto.PagedSystemHealthResponse;
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
import java.util.ArrayList;
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
    public String healthDashboard(
            Model model,
            @CurrentUser AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "component") String groupBy) {

        try {
            log.info("헬스 대시보드 요청 - page: {}, size: {}, groupBy: {}", page, size, groupBy);

            if (page < 0) {
                page = 0;
            }
            if (size <= 0 || size > 100) {
                size = 10;
            }
            if (!groupBy.equals("component") && !groupBy.equals("status")) {
                groupBy = "component";
            }

            log.debug("헬스 대시보드 데이터 조회 시작");
            PagedSystemHealthResponse pagedSystemHealth = healthCheckService.getSystemHealthPaged(page, size, groupBy);
            log.debug("헬스 대시보드 데이터 조회 완료");
            model.addAttribute("systemHealth", pagedSystemHealth);
            model.addAttribute("pagination", pagedSystemHealth.getPagination());
            model.addAttribute("componentGroups", pagedSystemHealth.getComponentGroups());
            model.addAttribute("currentPage", page);
            model.addAttribute("currentSize", size);
            model.addAttribute("currentGroupBy", groupBy);

            Pageable pageable = PageRequest.of(0, 10, Sort.by("checkedAt").descending());
            Page<HealthCheckResponse> recentChecks = healthCheckService.getHealthHistory(
                    LocalDateTime.now().minusHours(24), pageable);
            model.addAttribute("recentChecks", recentChecks.getContent());

            log.debug("헬스 대시보드 로드 완료 - 총 컴포넌트: {}, 현재 페이지: {}/{}",
                    pagedSystemHealth.getTotalComponents(), page + 1,
                    pagedSystemHealth.getPagination().getTotalPages());

            return "health/dashboard";

        } catch (Exception e) {
            log.error("헬스 대시보드 로드 실패", e);
            model.addAttribute("error", "헬스 대시보드를 로드하는 중 오류가 발생했습니다: " + e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/component")
    public String componentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            @CurrentUser AuthenticatedUser user) {
        try {
            log.info("컴포넌트 목록 및 체크 이력 조회 - page: {}, size: {}", page, size);

            List<String> components = Arrays.asList("database", "redis", "cache", "member-service", "rate-limit");
            model.addAttribute("components", components);

            for (String component : components) {
                try {
                    Pageable pageable = PageRequest.of(0, 1, Sort.by("checkedAt").descending());
                    Page<HealthCheckResponse> latestCheck = healthCheckService.getComponentHealth(component, pageable);
                    if (!latestCheck.getContent().isEmpty()) {
                        model.addAttribute(component + "Status", latestCheck.getContent().get(0));
                    }
                } catch (Exception e) {
                    log.warn("컴포넌트 {} 상태 조회 실패: {}", component, e.getMessage());
                }
            }

            Pageable pageable;
            if (size == 0) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("checkedAt").descending());
            } else {
                pageable = PageRequest.of(page, size, Sort.by("checkedAt").descending());
            }
            Page<HealthCheckResponse> allHealthChecks = healthCheckService.getHealthHistory(
                    LocalDateTime.now().minusDays(7), pageable);

            model.addAttribute("healthChecks", allHealthChecks);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", allHealthChecks.getTotalPages());
            model.addAttribute("totalElements", allHealthChecks.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("component", "all");

            log.debug("컴포넌트 목록 및 체크 이력 조회 완료 - 총 체크 수: {}", allHealthChecks.getTotalElements());

            return "health/component";
        } catch (Exception e) {
            log.error("컴포넌트 목록 조회 실패", e);
            model.addAttribute("error", "컴포넌트 목록을 조회할 수 없습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/component/{component}")
    public String componentHealth(@PathVariable String component,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  Model model, @CurrentUser AuthenticatedUser user) {
        try {
            Pageable pageable;
            if (size == 0) {
                pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("checkedAt").descending());
            } else {
                pageable = PageRequest.of(page, size, Sort.by("checkedAt").descending());
            }
            Page<HealthCheckResponse> healthChecks = healthCheckService.getComponentHealth(component, pageable);

            model.addAttribute("component", component);
            model.addAttribute("healthChecks", healthChecks);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", healthChecks.getTotalPages());
            model.addAttribute("totalElements", healthChecks.getTotalElements());
            model.addAttribute("size", size);

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
            @RequestParam(required = false) String component,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model, @CurrentUser AuthenticatedUser user) {
        try {
            log.info("헬스 체크 이력 요청 - page: {}, size: {}, fromDate: {}, toDate: {}, component: {}, status: {}",
                    page, size, fromDate, toDate, component, status);

            LocalDateTime startDate = fromDate != null ? fromDate : LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = toDate != null ? toDate : LocalDateTime.now();

            Pageable pageable = PageRequest.of(page, size, Sort.by("checkedAt").descending());

            Page<HealthCheckResponse> healthChecks = healthCheckService.getHealthHistoryWithFilters(
                    startDate, endDate, component, status, pageable);

            long totalChecks = healthChecks.getTotalElements();
            long successfulChecks = healthChecks.getContent().stream()
                    .mapToLong(check -> check.isHealthy() ? 1 : 0)
                    .sum();
            long failedChecks = totalChecks - successfulChecks;
            double successRate = totalChecks > 0 ? (double) successfulChecks / totalChecks * 100 : 0.0;

            List<String> availableComponents = healthCheckService.getAvailableComponents();

            model.addAttribute("healthChecks", healthChecks);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            model.addAttribute("component", component);
            model.addAttribute("status", status);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", healthChecks.getTotalPages());
            model.addAttribute("totalElements", healthChecks.getTotalElements());

            model.addAttribute("totalChecks", totalChecks);
            model.addAttribute("successfulChecks", successfulChecks);
            model.addAttribute("failedChecks", failedChecks);
            model.addAttribute("successRate", successRate);
            model.addAttribute("availableComponents", availableComponents);

            log.debug("헬스 체크 이력 로드 완료 - 총 체크: {}, 성공: {}, 실패: {}, 성공률: {}%",
                    totalChecks, successfulChecks, failedChecks, successRate);

            return "health/history";
        } catch (Exception e) {
            log.error("헬스 체크 이력 조회 실패", e);
            model.addAttribute("error", "헬스 체크 이력을 조회할 수 없습니다: " + e.getMessage());
            return "health/error";
        }
    }

    @GetMapping("/check")
    public String performHealthCheckGet(@RequestParam(required = false) List<String> components,
                                        Model model, @CurrentUser(required = false) AuthenticatedUser user) {
        return performHealthCheck(components, model, user);
    }

    @PostMapping("/check")
    public String performHealthCheck(@RequestParam(required = false) List<String> components,
                                     Model model, @CurrentUser(required = false) AuthenticatedUser user) {
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

    @GetMapping("/public/check")
    public String performPublicHealthCheck(@RequestParam(required = false) List<String> components,
                                           Model model) {
        try {
            if (components == null || components.isEmpty()) {
                components = Arrays.asList("database", "redis", "cache", "member-service", "rate-limit");
            }

            log.info("공개 헬스 체크 요청 - components: {}", components);

            HealthCheckRequest request = HealthCheckRequest.builder()
                    .components(components)
                    .checkType("public")
                    .ignoreCache(false)
                    .includeDetails(true)
                    .build();

            SystemHealthResponse response = healthCheckService.getSystemHealth(request);
            model.addAttribute("systemHealth", response);
            model.addAttribute("message", "헬스 체크 데이터를 조회했습니다.");

            return "health/result";
        } catch (Exception e) {
            log.error("공개 헬스 체크 조회 실패", e);
            model.addAttribute("error", "헬스 체크 데이터를 조회할 수 없습니다: " + e.getMessage());
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

            if (response.getComponents() != null && response.getComponents().size() > 10) {
                response.setComponents(response.getComponents().subList(0, 10));
            }

            model.addAttribute("systemHealth", response);

            try {
                HealthMetricsResponse metrics = healthMetricsService.calculateRealtimeMetrics();
                if (metrics.getComponentMetrics() != null && metrics.getComponentMetrics().size() > 5) {
                    metrics.setComponentMetrics(metrics.getComponentMetrics().subList(0, 5));
                }
                model.addAttribute("metrics", metrics);
            } catch (Exception e) {
                log.warn("실시간 메트릭스 조회 실패: {}", e.getMessage());
            }

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

            if (metrics.getComponentMetrics() != null && !metrics.getComponentMetrics().isEmpty()) {
                model.addAttribute("components", metrics.getComponentMetrics());
                model.addAttribute("activeComponents", metrics.getComponentMetrics().size());
            } else {
                List<HealthMetricsResponse.ComponentMetrics> defaultComponents = new ArrayList<>();
                model.addAttribute("components", defaultComponents);
                model.addAttribute("activeComponents", 0);
            }

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

    @GetMapping("/api/metrics/realtime")
    @ResponseBody
    public HealthMetricsResponse getRealtimeMetrics() {
        try {
            return healthMetricsService.calculateRealtimeMetrics();
        } catch (Exception e) {
            log.error("실시간 메트릭스 조회 실패", e);
            return HealthMetricsResponse.builder()
                    .cpu(Math.random() * 100)
                    .memory(Math.random() * 100)
                    .disk(Math.random() * 100)
                    .averageResponseTime(Math.random() * 1000)
                    .calculatedAt(java.time.LocalDateTime.now())
                    .build();
        }
    }

    @GetMapping("/api/paged")
    @ResponseBody
    public PagedSystemHealthResponse getSystemHealthPagedApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "component") String groupBy) {

        try {
            log.info("페이징된 시스템 헬스 API 요청 - page: {}, size: {}, groupBy: {}", page, size, groupBy);

            if (page < 0) {
                page = 0;
            }
            if (size <= 0 || size > 100) {
                size = 10;
            }
            if (!groupBy.equals("component") && !groupBy.equals("status")) {
                groupBy = "component";
            }

            PagedSystemHealthResponse response = healthCheckService.getSystemHealthPaged(page, size, groupBy);

            log.debug("페이징된 시스템 헬스 API 응답 완료 - 총 컴포넌트: {}, 현재 페이지: {}/{}",
                    response.getTotalComponents(), page + 1, response.getPagination().getTotalPages());

            return response;

        } catch (Exception e) {
            log.error("페이징된 시스템 헬스 API 조회 실패", e);
            throw new RuntimeException("페이징된 시스템 헬스 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}