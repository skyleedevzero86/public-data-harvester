package com.antock.global.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class SlowQueryInterceptor implements StatementInspector {

    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    private final AtomicLong slowQueryCount = new AtomicLong(0);
    private final AtomicLong totalQueryCount = new AtomicLong(0);

    @Override
    public String inspect(String sql) {
        totalQueryCount.incrementAndGet();

        if (sql != null && sql.trim().length() > 0) {
            log.debug("SQL Query: {}", sql);
        }

        return sql;
    }

    public void recordSlowQuery(long executionTimeMs, String sql) {
        if (executionTimeMs > SLOW_QUERY_THRESHOLD_MS) {
            slowQueryCount.incrementAndGet();
            log.warn("Slow query detected: {}ms - {}", executionTimeMs, sql);
        }
    }

    public long getSlowQueryCount() {
        return slowQueryCount.get();
    }

    public long getTotalQueryCount() {
        return totalQueryCount.get();
    }

    public double getSlowQueryPercentage() {
        long total = totalQueryCount.get();
        if (total == 0) return 0.0;
        return (double) slowQueryCount.get() / total * 100.0;
    }

    public void resetCounters() {
        slowQueryCount.set(0);
        totalQueryCount.set(0);
    }
}