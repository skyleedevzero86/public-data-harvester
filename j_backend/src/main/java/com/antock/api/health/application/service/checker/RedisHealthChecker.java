package com.antock.api.health.application.service.checker;

import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisHealthChecker implements ComponentHealthChecker {

    @Autowired(required = false)
    private JedisPool jedisPool;

    @Override
    public String getComponentName() {
        return "redis";
    }

    @Override
    public HealthCheckResult check() {
        Optional<JedisPool> pool = Optional.ofNullable(jedisPool);
        
        if (pool.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("reason", "Redis가 비활성화되어 있습니다");
            details.put("enabled", false);
            return new HealthCheckResult(HealthStatus.UNKNOWN, "Redis 비활성화됨", details, 0L);
        }

        try (Jedis jedis = pool.get().getResource()) {
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
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("error", "PING_RESPONSE_MISMATCH");
                errorDetails.put("errorMessage", "Redis ping 응답이 예상과 다릅니다: " + pong);
                return new HealthCheckResult(HealthStatus.DOWN, "Redis ping 실패", errorDetails, responseTime);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "Redis 연결 실패: " + e.getMessage(), details, 0L);
        }
    }
}

