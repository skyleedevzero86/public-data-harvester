package com.antock.api.health.application.service.checker;

import com.antock.api.health.domain.HealthCheckResult;
import com.antock.api.health.domain.HealthStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseHealthChecker implements ComponentHealthChecker {

    private final EntityManager entityManager;

    @Override
    public String getComponentName() {
        return "database";
    }

    @Override
    public HealthCheckResult check() {
        try {
            long startTime = System.currentTimeMillis();
            Query query = entityManager.createNativeQuery("SELECT 1");
            Integer result = ((Number) query.getSingleResult()).intValue();
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> details = new HashMap<>();
            details.put("responseTime", responseTime);
            details.put("queryResult", result);

            if (result != null && result == 1) {
                return new HealthCheckResult(HealthStatus.UP, "데이터베이스 연결 정상", details, responseTime);
            } else {
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("error", "QUERY_RESULT_MISMATCH");
                errorDetails.put("errorMessage", "데이터베이스 쿼리 결과가 예상과 다릅니다");
                return new HealthCheckResult(HealthStatus.DOWN, "데이터베이스 쿼리 실패", errorDetails, responseTime);
            }
        } catch (Exception e) {
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getClass().getSimpleName());
            details.put("errorMessage", e.getMessage());
            return new HealthCheckResult(HealthStatus.DOWN, "데이터베이스 연결 실패: " + e.getMessage(), details, 0L);
        }
    }
}

