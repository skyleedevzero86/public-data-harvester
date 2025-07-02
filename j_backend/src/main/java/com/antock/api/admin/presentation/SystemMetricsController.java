package com.antock.api.admin.presentation;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.MemberCacheService;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemMetricsController {

    private final MemberApplicationService memberApplicationService;
    private final RateLimitServiceInterface rateLimitService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSystemSummary() {
        Map<String, Object> summary = new HashMap<>();

        MemberCacheService.CacheStatistics cacheStats = memberApplicationService.getCacheStatistics();
        Map<String, Object> cache = new HashMap<>();
        cache.put("available", cacheStats.isCacheAvailable());
        cache.put("hitRate", cacheStats.getHitRate());
        cache.put("totalRequests", cacheStats.getTotalRequests());
        cache.put("errors", cacheStats.getCacheErrors());

        Map<String, Object> rateLimit = new HashMap<>();
        rateLimit.put("redisAvailable", rateLimitService.isRedisAvailable());
        rateLimit.put("backend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory");

        Map<String, Object> system = new HashMap<>();
        system.put("timestamp", System.currentTimeMillis());
        system.put("uptime", getSystemUptime());
        system.put("memoryUsage", getMemoryUsage());

        summary.put("cache", cache);
        summary.put("rateLimit", rateLimit);
        summary.put("system", system);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/cache/performance")
    public ResponseEntity<Map<String, Object>> getCachePerformanceMetrics() {
        MemberCacheService.CacheStatistics stats = memberApplicationService.getCacheStatistics();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("hitRate", stats.getHitRate());
        metrics.put("missRate", 100.0 - stats.getHitRate());
        metrics.put("totalRequests", stats.getTotalRequests());
        metrics.put("cacheHits", stats.getCacheHits());
        metrics.put("cacheMisses", stats.getCacheMisses());
        metrics.put("errorRate", stats.getTotalRequests() > 0 ?
                (double) stats.getCacheErrors() / stats.getTotalRequests() * 100 : 0);
        metrics.put("isHealthy", stats.isCacheAvailable() && stats.getCacheErrors() < 10);

        return ResponseEntity.ok(metrics);
    }

    private long getSystemUptime() {
        return System.currentTimeMillis() -
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
    }

    private Map<String, Object> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemory", runtime.totalMemory());
        memory.put("freeMemory", runtime.freeMemory());
        memory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memory.put("maxMemory", runtime.maxMemory());
        return memory;
    }
}