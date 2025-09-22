package com.antock.api.health.presentation;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.application.dto.HealthMetricsResponse;
import com.antock.api.health.application.service.HealthCheckService;
import com.antock.api.health.application.service.HealthMetricsService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "시스템 헬스 체크 API")
public class HealthApiController {

    private static final Logger log = LoggerFactory.getLogger(HealthApiController.class);

    private final HealthCheckService healthCheckService;
    private final HealthMetricsService healthMetricsService;

    @GetMapping("/system")
    @Operation(summary = "시스템 전체 헬스 상태 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "헬스 상태 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.antock.global.common.response.ApiResponse.class),
                            examples = @ExampleObject(name = "시스템 헬스 상태",
                                    value = "{\"success\": true, \"message\": \"시스템 헬스 상태 조회 완료\", \"data\": {\"overallStatus\": \"UP\", \"overallStatusDescription\": \"정상\", \"totalComponents\": 5, \"healthyComponents\": 5, \"unhealthyComponents\": 0, \"unknownComponents\": 0, \"healthPercentage\": 100.0, \"checkedAt\": \"2024-01-15T10:30:00\", \"expiresAt\": \"2024-01-15T10:35:00\", \"expired\": false, \"healthy\": true, \"components\": [{\"component\": \"database\", \"status\": \"UP\", \"statusDescription\": \"정상\", \"message\": \"데이터베이스 연결 정상\", \"responseTime\": 150, \"checkType\": \"scheduled\", \"checkedAt\": \"2024-01-15T10:30:00\", \"expired\": false, \"healthy\": true}]}, \"timestamp\": \"2024-01-15T10:30:00\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.antock.global.common.response.ApiResponse<SystemHealthResponse>> getSystemHealth(
            @Parameter(description = "캐시 무시 여부", example = "false") @RequestParam(defaultValue = "false") boolean ignoreCache,
            @Parameter(description = "체크할 컴포넌트 목록 (쉼표로 구분)", example = "database,redis,cache") @RequestParam(required = false) List<String> components,
            @Parameter(description = "체크 타입", example = "manual") @RequestParam(defaultValue = "api") String checkType) {
        try {
            HealthCheckRequest request = HealthCheckRequest.builder()
                    .components(components)
                    .checkType(checkType)
                    .ignoreCache(ignoreCache)
                    .includeDetails(true)
                    .build();

            SystemHealthResponse response = healthCheckService.getSystemHealth(request);

            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, response));
        } catch (Exception e) {
            log.error("시스템 헬스 상태 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("시스템 헬스 상태 조회 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/check")
    @Operation(summary = "수동 헬스 체크 실행")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "헬스 체크 실행 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.antock.global.common.response.ApiResponse<SystemHealthResponse>> performHealthCheck(
            @RequestBody HealthCheckRequest request) {
        try {
            request.setIgnoreCache(true);
            request.setCheckType("manual");

            SystemHealthResponse response = healthCheckService.performSystemHealthCheck(
                    request.getComponents(), request.getCheckType());

            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, response));
        } catch (Exception e) {
            log.error("수동 헬스 체크 실행 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("헬스 체크 실행 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/component/{component}")
    @Operation(summary = "특정 컴포넌트 헬스 상태 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "컴포넌트 헬스 상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "컴포넌트를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.antock.global.common.response.ApiResponse<Page<HealthCheckResponse>>> getComponentHealth(
            @Parameter(description = "컴포넌트명", example = "database", required = true) @PathVariable String component,
            Pageable pageable) {
        try {
            Page<HealthCheckResponse> response = healthCheckService.getComponentHealth(component, pageable);
            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, response));
        } catch (Exception e) {
            log.error("컴포넌트 헬스 상태 조회 실패: {}", component, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("컴포넌트 헬스 상태 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "헬스 체크 이력 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "헬스 체크 이력 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.antock.global.common.response.ApiResponse<Page<HealthCheckResponse>>> getHealthHistory(
            @Parameter(description = "시작 날짜", example = "2024-01-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "종료 날짜", example = "2024-01-31T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Pageable pageable) {
        try {
            LocalDateTime startDate = fromDate != null ? fromDate : LocalDateTime.now().minusDays(7);
            Page<HealthCheckResponse> response = healthCheckService.getHealthHistory(startDate, pageable);
            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, response));
        } catch (Exception e) {
            log.error("헬스 체크 이력 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("헬스 체크 이력 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "간단한 헬스 상태 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "헬스 상태 조회 성공"),
            @ApiResponse(responseCode = "503", description = "서비스 사용 불가")
    })
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        try {
            HealthCheckRequest request = HealthCheckRequest.builder()
                    .ignoreCache(false)
                    .includeDetails(false)
                    .build();

            SystemHealthResponse response = healthCheckService.getSystemHealth(request);

            Map<String, Object> status = Map.of(
                    "status", response.getOverallStatus().getCode(),
                    "healthy", response.isHealthy(),
                    "timestamp", LocalDateTime.now());

            if (response.isHealthy()) {
                return ResponseEntity.ok(status);
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
            }
        } catch (Exception e) {
            log.error("간단한 헬스 상태 조회 실패", e);
            Map<String, Object> errorStatus = Map.of(
                    "status", "DOWN",
                    "healthy", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorStatus);
        }
    }

    @PostMapping("/cleanup")
    @Operation(summary = "만료된 헬스 체크 데이터 정리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 정리 완료"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.antock.global.common.response.ApiResponse<Map<String, Object>>> cleanupExpiredData() {
        try {
            healthCheckService.cleanupExpiredChecks();

            Map<String, Object> result = Map.of(
                    "message", "만료된 헬스 체크 데이터 정리 완료",
                    "timestamp", LocalDateTime.now());

            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, result));
        } catch (Exception e) {
            log.error("헬스 체크 데이터 정리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("데이터 정리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/metrics")
    @Operation(summary = "헬스 체크 상세 메트릭 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메트릭 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.antock.global.common.response.ApiResponse<HealthMetricsResponse>> getHealthMetrics(
            @Parameter(description = "조회 기간 (일)", example = "7") @RequestParam(defaultValue = "7") int days) {
        try {
            HealthMetricsResponse metrics = healthMetricsService.calculateSystemMetrics(days);
            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, metrics));
        } catch (Exception e) {
            log.error("헬스 체크 메트릭 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("메트릭 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/metrics/component/{component}")
    @Operation(summary = "컴포넌트별 상세 메트릭 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "컴포넌트 메트릭 조회 성공"),
            @ApiResponse(responseCode = "404", description = "컴포넌트를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.antock.global.common.response.ApiResponse<HealthMetricsResponse.ComponentMetrics>> getComponentMetrics(
            @Parameter(description = "컴포넌트명", example = "database") @PathVariable String component,
            @Parameter(description = "조회 기간 (일)", example = "7") @RequestParam(defaultValue = "7") int days) {
        try {
            HealthMetricsResponse.ComponentMetrics metrics = healthMetricsService.calculateComponentMetrics(component, days);
            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, metrics));
        } catch (Exception e) {
            log.error("컴포넌트 메트릭 조회 실패: {}", component, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("컴포넌트 메트릭 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/metrics/realtime")
    @Operation(summary = "실시간 메트릭 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "실시간 메트릭 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.antock.global.common.response.ApiResponse<HealthMetricsResponse>> getRealtimeMetrics() {
        try {
            HealthMetricsResponse metrics = healthMetricsService.calculateRealtimeMetrics();
            return ResponseEntity.ok(com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, metrics));
        } catch (Exception e) {
            log.error("실시간 메트릭 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.antock.global.common.response.ApiResponse.error("실시간 메트릭 조회 중 오류가 발생했습니다."));
        }
    }
}