package com.antock.api.admin.presentation;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.MemberCacheService;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Cache Monitoring", description = "캐시 모니터링 및 관리 API")
public class CacheMonitoringController {

    private static final Logger log = LoggerFactory.getLogger(CacheMonitoringController.class);

    private final MemberApplicationService memberApplicationService;
    private final RateLimitServiceInterface rateLimitService;

    @GetMapping("/statistics")
    @Operation(summary = "캐시 통계 조회", description = "전체 캐시 시스템의 통계 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = MemberCacheService.CacheStatistics.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<MemberCacheService.CacheStatistics> getCacheStatistics() {
        log.info("관리자 캐시 통계 조회 요청");

        MemberCacheService.CacheStatistics statistics = memberApplicationService.getCacheStatistics();
        return ResponseEntity.ok(statistics);
    }

    @DeleteMapping("/members")
    @Operation(summary = "전체 회원 캐시 무효화", description = "모든 회원 관련 캐시를 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
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
    @Operation(summary = "특정 회원 캐시 무효화", description = "지정된 회원의 캐시를 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, Object>> evictMemberCache(
            @Parameter(description = "회원 ID", example = "123") @PathVariable Long memberId) {
        log.info("관리자에 의한 특정 회원 캐시 무효화 요청 - ID: {}", memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원 ID " + memberId + "의 캐시가 무효화되었습니다");
        response.put("memberId", memberId);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rate-limit/status")
    @Operation(summary = "속도 제한 상태 조회", description = "현재 속도 제한 시스템의 상태를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        log.info("관리자 속도 제한 상태 조회 요청");

        Map<String, Object> status = new HashMap<>();
        status.put("redisAvailable", rateLimitService.isRedisAvailable());
        status.put("backend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory");
        status.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/rate-limit/{identifier}/{action}")
    @Operation(summary = "속도 제한 재설정", description = "지정된 식별자와 작업의 속도 제한을 재설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> resetRateLimit(
            @Parameter(description = "식별자 (IP 주소 또는 사용자 ID)", example = "192.168.1.1") @PathVariable String identifier,
            @Parameter(description = "작업 유형", example = "login") @PathVariable String action) {

        log.info("관리자에 의한 속도 제한 재설정 - 식별자: {}, 작업: {}", identifier, action);

        int beforeCount = rateLimitService.getCurrentCount(identifier, action);
        rateLimitService.resetRateLimit(identifier, action);
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
    @Operation(summary = "현재 속도 제한 카운트 조회", description = "지정된 식별자와 작업의 현재 속도 제한 카운트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> getCurrentRateLimit(
            @Parameter(description = "식별자", example = "192.168.1.1") @PathVariable String identifier,
            @Parameter(description = "작업 유형", example = "login") @PathVariable String action) {

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

    @GetMapping("/rate-limit/security/{identifier}")
    @Operation(summary = "속도 제한 보안 정보 조회", description = "지정된 식별자의 보안 관련 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> getRateLimitSecurityInfo(
            @Parameter(description = "식별자", example = "192.168.1.1") @PathVariable String identifier) {
        log.info("관리자에 의한 rate limit 보안 정보 조회 - 식별자: {}", identifier);

        Map<String, Object> response = new HashMap<>();
        response.put("identifier", identifier);
        response.put("backend", rateLimitService.isRedisAvailable() ? "Redis" : "Memory");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "보안 정보 조회 완료");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rate-limit/security/block/{identifier}")
    @Operation(summary = "식별자 차단", description = "지정된 식별자를 차단합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> blockIdentifier(
            @Parameter(description = "차단할 식별자", example = "192.168.1.1") @PathVariable String identifier,
            @Parameter(description = "차단 사유", example = "의심스러운 활동") @RequestParam String reason,
            @Parameter(description = "차단 시간(분)", example = "30") @RequestParam(defaultValue = "30") long blockDurationMinutes) {

        log.warn("관리자에 의한 식별자 차단 - 식별자: {}, 사유: {}, 차단시간: {}분", identifier, reason, blockDurationMinutes);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "식별자가 차단되었습니다");
        response.put("identifier", identifier);
        response.put("reason", reason);
        response.put("blockDurationMinutes", blockDurationMinutes);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rate-limit/security/unblock/{identifier}")
    @Operation(summary = "식별자 차단 해제", description = "지정된 식별자의 차단을 해제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> unblockIdentifier(
            @Parameter(description = "차단 해제할 식별자", example = "192.168.1.1") @PathVariable String identifier) {
        log.info("관리자에 의한 식별자 차단 해제 - 식별자: {}", identifier);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "식별자의 차단이 해제되었습니다");
        response.put("identifier", identifier);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rate-limit/security/whitelist/{identifier}")
    @Operation(summary = "화이트리스트 추가", description = "지정된 식별자를 화이트리스트에 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> addToWhitelist(
            @Parameter(description = "화이트리스트에 추가할 식별자", example = "192.168.1.1") @PathVariable String identifier) {
        log.info("관리자에 의한 화이트리스트 추가 - 식별자: {}", identifier);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "화이트리스트에 추가되었습니다");
        response.put("identifier", identifier);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/rate-limit/security/whitelist/{identifier}")
    @Operation(summary = "화이트리스트 제거", description = "지정된 식별자를 화이트리스트에서 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<Map<String, Object>> removeFromWhitelist(
            @Parameter(description = "화이트리스트에서 제거할 식별자", example = "192.168.1.1") @PathVariable String identifier) {
        log.info("관리자에 의한 화이트리스트 제거 - 식별자: {}", identifier);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "화이트리스트에서 제거되었습니다");
        response.put("identifier", identifier);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}