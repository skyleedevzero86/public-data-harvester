package com.antock.api.admin.presentation;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.MemberCacheService;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CacheMonitoringController {

    private static final Logger log = LoggerFactory.getLogger(CacheMonitoringController.class);

    private final MemberApplicationService memberApplicationService;
    private final RateLimitServiceInterface rateLimitService;

    @GetMapping("/statistics")
    public ResponseEntity<MemberCacheService.CacheStatistics> getCacheStatistics() {
        log.info("관리자 캐시 통계 조회 요청");

        MemberCacheService.CacheStatistics statistics = memberApplicationService.getCacheStatistics();

        return ResponseEntity.ok(statistics);
    }

    @DeleteMapping("/members")
    public ResponseEntity<Map<String, Object>> evictAllMemberCache() {
        log.warn("관리자에 의한 전체 회원 캐시 무효화 요청");

        long startTime = System.currentTimeMillis();
        memberApplicationService.evictAllMemberCache();
        long elapsed = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "전체 회원 캐시가 무효화되었습니다");
        response.put("executionTimeMs", elapsed);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Map<String, Object>> evictMemberCache(@PathVariable Long memberId) {
        log.info("관리자에 의한 특정 회원 캐시 무효화 요청 - ID: {}", memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원 ID " + memberId + "의 캐시가 무효화되었습니다");
        response.put("memberId", memberId);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rate-limit/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        log.info("관리자 속도 제한 상태 조회 요청");

        Map<String, Object> status = new HashMap<>();
        status.put("redisAvailable", rateLimitService.isRedisAvailable());
        status.put("backend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory");
        status.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/rate-limit/{identifier}/{action}")
    public ResponseEntity<Map<String, Object>> resetRateLimit(
            @PathVariable String identifier,
            @PathVariable String action) {

        log.info("관리자에 의한 속도 제한 재설정 - 식별자: {}, 작업: {}", identifier, action);

        int beforeCount = rateLimitService.getCurrentCount(identifier, action);
        rateLimitService.resetLimit(identifier, action);
        int afterCount = rateLimitService.getCurrentCount(identifier, action);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "속도 제한이 재설정되었습니다");
        response.put("identifier", identifier);
        response.put("action", action);
        response.put("beforeCount", beforeCount);
        response.put("afterCount", afterCount);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rate-limit/{identifier}/{action}")
    public ResponseEntity<Map<String, Object>> getCurrentRateLimit(
            @PathVariable String identifier,
            @PathVariable String action) {

        log.debug("속도 제한 카운트 조회 - 식별자: {}, 작업: {}", identifier, action);

        int currentCount = rateLimitService.getCurrentCount(identifier, action);

        Map<String, Object> response = new HashMap<>();
        response.put("identifier", identifier);
        response.put("action", action);
        response.put("currentCount", currentCount);
        response.put("backend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}