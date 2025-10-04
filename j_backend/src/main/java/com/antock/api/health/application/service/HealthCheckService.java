package com.antock.api.health.application.service;

import com.antock.api.health.application.dto.HealthCheckRequest;
import com.antock.api.health.application.dto.HealthCheckResponse;
import com.antock.api.health.application.dto.SystemHealthResponse;
import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import com.antock.api.health.domain.SystemHealth;
import com.antock.api.health.infrastructure.HealthCheckRepository;
import com.antock.api.health.infrastructure.SystemHealthRepository;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.RateLimitServiceInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HealthCheckService {

    private final HealthCheckRepository healthCheckRepository;
    private final SystemHealthRepository systemHealthRepository;
    private final DataSource dataSource;
    @Autowired(required = false)
    private JedisPool jedisPool;
    private final MemberApplicationService memberApplicationService;
    private final RateLimitServiceInterface rateLimitService;
    private final ObjectMapper objectMapper;
    private final Executor asyncExecutor;

    public HealthCheckService(HealthCheckRepository healthCheckRepository,
                             SystemHealthRepository systemHealthRepository,
                             DataSource dataSource,
                             MemberApplicationService memberApplicationService,
                             RateLimitServiceInterface rateLimitService,
                             ObjectMapper objectMapper,
                             Executor asyncExecutor) {
        this.healthCheckRepository = healthCheckRepository;
        this.systemHealthRepository = systemHealthRepository;
        this.dataSource = dataSource;
        this.memberApplicationService = memberApplicationService;
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.asyncExecutor = asyncExecutor;
    }

    @Value("${health.check.timeout:5000}")
    private long healthCheckTimeout;

    @Value("${health.cache.duration:300}")
    private long cacheDurationSeconds;

    private static final List<String> DEFAULT_COMPONENTS = Arrays.asList(
            "database", "redis", "cache", "member-service", "rate-limit");

    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth(HealthCheckRequest request) {
        List<String> components = request.getComponents() != null ? request.getComponents() : DEFAULT_COMPONENTS;

        if (request.isIgnoreCache()) {
            return performSystemHealthCheckInNewTransaction(components, request.getCheckType());
        }

        Optional<SystemHealth> cachedSystemHealth = systemHealthRepository
                .findLatestValidSystemHealth(LocalDateTime.now());
        if (cachedSystemHealth.isPresent() && !cachedSystemHealth.get().isExpired()) {
            return convertToSystemHealthResponseFromComponents(cachedSystemHealth.get(), components);
        }

        return performSystemHealthCheckInNewTransaction(components, request.getCheckType());
    }

    @Transactional
    public SystemHealthResponse performSystemHealthCheck(List<String> components, String checkType) {
        List<CompletableFuture<HealthCheck>> futures = components.stream()
                .map(component -> CompletableFuture.supplyAsync(() -> performComponentHealthCheck(component, checkType),
                        asyncExecutor))
                .collect(Collectors.toList());

        List<HealthCheck> healthChecks = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        SystemHealth systemHealth = calculateSystemHealth(healthChecks, checkType);
        systemHealthRepository.save(systemHealth);

        healthCheckRepository.saveAll(healthChecks);

        return convertToSystemHealthResponse(systemHealth, healthChecks);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SystemHealthResponse performSystemHealthCheckInNewTransaction(List<String> components, String checkType) {
        return performSystemHealthCheck(components, checkType);
    }

    @Transactional
    public HealthCheck performComponentHealthCheck(String component, String checkType) {
        long startTime = System.currentTimeMillis();
        HealthStatus status = HealthStatus.UNKNOWN;
        String message = "";
        Map<String, Object> details = new HashMap<>();

        try {
            switch (component.toLowerCase()) {
                case "database":
                    HealthCheckResult dbResult = checkDatabase();
                    status = dbResult.getStatus();
                    message = dbResult.getMessage();
                    details = dbResult.getDetails();
                    break;
                case "redis":
                    HealthCheckResult redisResult = checkRedis();
                    status = redisResult.getStatus();
                    message = redisResult.getMessage();
                    details = redisResult.getDetails();
                    break;
                case "cache":
                    HealthCheckResult cacheResult = checkCache();
                    status = cacheResult.getStatus();
                    message = cacheResult.getMessage();
                    details = cacheResult.getDetails();
                    break;
                case "member-service":
                    HealthCheckResult memberResult = checkMemberService();
                    status = memberResult.getStatus();
                    message = memberResult.getMessage();
                    details = memberResult.getDetails();
                    break;
                case "rate-limit":
                    HealthCheckResult rateLimitResult = checkRateLimit();
                    status = rateLimitResult.getStatus();
                    message = rateLimitResult.getMessage();
                    details = rateLimitResult.getDetails();
                    break;
                default:
                    message = "알 수 없는 컴포넌트: " + component;
                    status = HealthStatus.UNKNOWN;
            }
        } catch (Exception e) {
            status = HealthStatus.DOWN;
            message = "헬스 체크 중 오류 발생: " + e.getMessage();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
        }

        long responseTime = System.currentTimeMillis() - startTime;

        return HealthCheck.builder()
                .component(component)
                .status(status)
                .message(message)
                .responseTime(responseTime)
                .checkType(checkType != null ? checkType : "manual")
                .details(convertDetailsToJson(details))
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(cacheDurationSeconds))
                .build();
    }

    private HealthCheckResult checkDatabase() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            long startTime = System.currentTimeMillis();
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> details = new HashMap<>();
            details.put("responseTime", responseTime);
            details.put("queryResult", result);

            if (result != null && result == 1) {
                return new HealthCheckResult(HealthStatus.UP, "데이터베이스 연결 정상", details, responseTime);
            } else {
                return new HealthCheckResult(HealthStatus.DOWN, "데이터베이스 쿼리 실패", details, responseTime);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "데이터베이스 연결 실패: " + e.getMessage(), details, 0L);
        }
    }

    private HealthCheckResult checkRedis() {
        if (jedisPool == null) {
            Map<String, Object> details = new HashMap<>();
            details.put("reason", "Redis가 비활성화되어 있습니다");
            details.put("enabled", false);
            return new HealthCheckResult(HealthStatus.UNKNOWN, "Redis 비활성화됨", details, 0L);
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            long startTime = System.currentTimeMillis();
            String pong = jedis.ping();
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> details = new HashMap<>();
            details.put("responseTime", responseTime);
            details.put("pingResult", pong);
            details.put("info", jedis.info("server"));

            if ("PONG".equals(pong)) {
                return new HealthCheckResult(HealthStatus.UP, "Redis 연결 정상", details, responseTime);
            } else {
                return new HealthCheckResult(HealthStatus.DOWN, "Redis ping 실패", details, responseTime);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "Redis 연결 실패: " + e.getMessage(), details, 0L);
        }
    }

    private HealthCheckResult checkCache() {
        try {
            var cacheStats = memberApplicationService.getCacheStatistics();
            Map<String, Object> details = new HashMap<>();
            details.put("cacheStats", cacheStats);

            if (cacheStats != null) {
                return new HealthCheckResult(HealthStatus.UP, "캐시 서비스 정상", details, 0L);
            } else {
                return new HealthCheckResult(HealthStatus.DOWN, "캐시 서비스 응답 없음", details, 0L);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "캐시 서비스 오류: " + e.getMessage(), details, 0L);
        }
    }

    private HealthCheckResult checkMemberService() {
        try {
            Map<String, Object> memberStats = new HashMap<>();
            memberStats.put("cacheStats", memberApplicationService.getCacheStatistics());
            Map<String, Object> details = new HashMap<>();
            details.put("memberStats", memberStats);

            if (memberStats != null) {
                return new HealthCheckResult(HealthStatus.UP, "회원 서비스 정상", details, 0L);
            } else {
                return new HealthCheckResult(HealthStatus.DOWN, "회원 서비스 응답 없음", details, 0L);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "회원 서비스 오류: " + e.getMessage(), details, 0L);
        }
    }

    private HealthCheckResult checkRateLimit() {
        try {
            boolean rateLimitStatus = rateLimitService.isRedisAvailable();
            Map<String, Object> details = new HashMap<>();
            details.put("rateLimitStatus", rateLimitStatus);

            if (rateLimitStatus) {
                return new HealthCheckResult(HealthStatus.UP, "Rate Limit 서비스 정상", details, 0L);
            } else {
                return new HealthCheckResult(HealthStatus.DOWN, "Rate Limit 서비스 응답 없음", details, 0L);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "Rate Limit 서비스 오류: " + e.getMessage(), details, 0L);
        }
    }

    private SystemHealth calculateSystemHealth(List<HealthCheck> healthChecks, String checkType) {
        int totalComponents = healthChecks.size();
        int healthyComponents = (int) healthChecks.stream().filter(h -> HealthStatus.UP.equals(h.getStatus())).count();
        int unhealthyComponents = (int) healthChecks.stream().filter(h -> HealthStatus.DOWN.equals(h.getStatus()))
                .count();
        int unknownComponents = (int) healthChecks.stream().filter(h -> HealthStatus.UNKNOWN.equals(h.getStatus()))
                .count();

        HealthStatus overallStatus;
        if (unhealthyComponents > 0) {
            overallStatus = HealthStatus.DOWN;
        } else if (unknownComponents > 0) {
            overallStatus = HealthStatus.UNKNOWN;
        } else {
            overallStatus = HealthStatus.UP;
        }

        Map<String, Object> details = new HashMap<>();
        Map<String, Map<String, Object>> componentDetails = new HashMap<>();
        for (HealthCheck h : healthChecks) {
            Map<String, Object> componentInfo = new HashMap<>();
            componentInfo.put("status", h.getStatus());
            componentInfo.put("message", h.getMessage());
            componentInfo.put("responseTime", h.getResponseTime());
            componentDetails.put(h.getComponent(), componentInfo);
        }
        details.put("components", componentDetails);

        return SystemHealth.builder()
                .overallStatus(overallStatus)
                .totalComponents(totalComponents)
                .healthyComponents(healthyComponents)
                .unhealthyComponents(unhealthyComponents)
                .unknownComponents(unknownComponents)
                .details(convertDetailsToJson(details))
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(cacheDurationSeconds))
                .build();
    }

    @Transactional(readOnly = true)
    public List<HealthCheckResponse> getComponentHealth(String component) {
        List<HealthCheck> healthChecks = healthCheckRepository.findByComponentOrderByCheckedAtDesc(component);
        return healthChecks.stream()
                .map(this::convertToHealthCheckResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckResponse> getComponentHealth(String component, Pageable pageable) {
        Page<HealthCheck> healthChecks = healthCheckRepository.findByComponentOrderByCheckedAtDesc(component, pageable);
        return healthChecks.map(this::convertToHealthCheckResponse);
    }

    @Transactional(readOnly = true)
    public List<HealthCheckResponse> getHealthHistory(LocalDateTime fromDate) {
        List<HealthCheck> healthChecks = healthCheckRepository.findByCheckedAtAfterOrderByCheckedAtDesc(fromDate);
        return healthChecks.stream()
                .map(this::convertToHealthCheckResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<HealthCheckResponse> getHealthHistory(LocalDateTime fromDate, Pageable pageable) {
        Page<HealthCheck> healthChecks = healthCheckRepository.findRecentChecks(fromDate, pageable);
        return healthChecks.map(this::convertToHealthCheckResponse);
    }

    @Transactional
    public void cleanupExpiredChecks() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deletedCount = healthCheckRepository.deleteOldChecks(cutoffDate);
        int deletedSystemHealthCount = systemHealthRepository.deleteOldSystemHealth(cutoffDate);
    }

    private HealthCheckResponse convertToHealthCheckResponse(HealthCheck healthCheck) {
        return HealthCheckResponse.builder()
                .component(healthCheck.getComponent())
                .status(healthCheck.getStatus())
                .statusDescription(healthCheck.getStatus().getDescription())
                .message(healthCheck.getMessage())
                .responseTime(healthCheck.getResponseTime())
                .checkType(healthCheck.getCheckType())
                .details(parseDetailsFromJson(healthCheck.getDetails()))
                .checkedAt(healthCheck.getCheckedAt())
                .expiresAt(healthCheck.getExpiresAt())
                .expired(healthCheck.isExpired())
                .healthy(healthCheck.isUp())
                .build();
    }

    private SystemHealthResponse convertToSystemHealthResponseFromComponents(SystemHealth systemHealth,
                                                                             List<String> components) {
        List<HealthCheck> recentChecks = healthCheckRepository.findByComponentInOrderByCheckedAtDesc(components);

        return SystemHealthResponse.builder()
                .overallStatus(systemHealth.getOverallStatus())
                .overallStatusDescription(systemHealth.getOverallStatus().getDescription())
                .totalComponents(systemHealth.getTotalComponents())
                .healthyComponents(systemHealth.getHealthyComponents())
                .unhealthyComponents(systemHealth.getUnhealthyComponents())
                .unknownComponents(systemHealth.getUnknownComponents())
                .healthPercentage(systemHealth.getHealthPercentage())
                .details(parseDetailsFromJson(systemHealth.getDetails()))
                .components(recentChecks.stream().map(this::convertToHealthCheckResponse).collect(Collectors.toList()))
                .checkedAt(systemHealth.getCheckedAt())
                .expiresAt(systemHealth.getExpiresAt())
                .expired(systemHealth.isExpired())
                .healthy(systemHealth.isHealthy())
                .build();
    }

    private SystemHealthResponse convertToSystemHealthResponse(SystemHealth systemHealth,
                                                               List<HealthCheck> healthChecks) {
        return SystemHealthResponse.builder()
                .overallStatus(systemHealth.getOverallStatus())
                .overallStatusDescription(systemHealth.getOverallStatus().getDescription())
                .totalComponents(systemHealth.getTotalComponents())
                .healthyComponents(systemHealth.getHealthyComponents())
                .unhealthyComponents(systemHealth.getUnhealthyComponents())
                .unknownComponents(systemHealth.getUnknownComponents())
                .healthPercentage(systemHealth.getHealthPercentage())
                .details(parseDetailsFromJson(systemHealth.getDetails()))
                .components(healthChecks.stream().map(this::convertToHealthCheckResponse).collect(Collectors.toList()))
                .checkedAt(systemHealth.getCheckedAt())
                .expiresAt(systemHealth.getExpiresAt())
                .expired(systemHealth.isExpired())
                .healthy(systemHealth.isHealthy())
                .build();
    }

    private String convertDetailsToJson(Map<String, Object> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDetailsFromJson(String detailsJson) {
        try {
            if (detailsJson == null || detailsJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(detailsJson, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}