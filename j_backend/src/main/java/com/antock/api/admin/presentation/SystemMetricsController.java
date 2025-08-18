package com.antock.api.admin.presentation;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - System Metrics", description = "시스템 메트릭 및 성능 모니터링 API")
public class SystemMetricsController {

    private final MemberApplicationService memberApplicationService;
    private final RateLimitServiceInterface rateLimitService;

    @GetMapping("/summary")
    @Operation(summary = "시스템 요약 정보", description = "전체 시스템의 상태와 성능 지표를 요약하여 제공합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> getSystemSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("uptime", getSystemUptime());
        summary.put("memory", getMemoryUsage());
        summary.put("timestamp", System.currentTimeMillis());

        try {
            var cacheStats = memberApplicationService.getCacheStatistics();
            summary.put("cache", Map.of(
                    "hitRate", cacheStats.getHitRate(),
                    "totalRequests", cacheStats.getTotalRequests(),
                    "cacheHits", cacheStats.getCacheHits(),
                    "cacheMisses", cacheStats.getCacheMisses()));
        } catch (Exception e) {
            summary.put("cache", Map.of("error", "캐시 정보 조회 실패"));
        }

        summary.put("rateLimit", Map.of(
                "backend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory",
                "available", rateLimitService.isRedisAvailable()));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/cache/performance")
    @Operation(summary = "캐시 성능 메트릭", description = "캐시 시스템의 성능 지표를 상세히 제공합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> getCachePerformanceMetrics() {
        try {
            var stats = memberApplicationService.getCacheStatistics();
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("hitRate", stats.getHitRate());
            metrics.put("totalRequests", stats.getTotalRequests());
            metrics.put("cacheHits", stats.getCacheHits());
            metrics.put("cacheMisses", stats.getCacheMisses());
            metrics.put("cacheAvailable", stats.isCacheAvailable());
            metrics.put("cacheErrors", stats.getCacheErrors());
            metrics.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "캐시 성능 메트릭 조회 실패");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    @GetMapping("/rate-limit/performance")
    @Operation(summary = "속도 제한 성능 메트릭", description = "속도 제한 시스템의 성능 지표를 제공합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> getRateLimitPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("backend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory");
        metrics.put("redisAvailable", rateLimitService.isRedisAvailable());
        metrics.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/security/overview")
    @Operation(summary = "보안 개요", description = "시스템의 전반적인 보안 상태를 요약하여 제공합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> getSecurityOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("rateLimitBackend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory");
        overview.put("jwtEnabled", true);
        overview.put("securityLevel", "HIGH");
        overview.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(overview);
    }

    private long getSystemUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    private Map<String, Object> getMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        long usedNonHeap = memoryBean.getNonHeapMemoryUsage().getUsed();

        return Map.of(
                "heapUsed", usedHeap,
                "heapMax", maxHeap,
                "heapUsagePercent", (double) usedHeap / maxHeap * 100,
                "nonHeapUsed", usedNonHeap,
                "totalUsed", usedHeap + usedNonHeap);
    }
}