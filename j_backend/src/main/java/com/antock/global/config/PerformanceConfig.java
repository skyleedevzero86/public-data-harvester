package com.antock.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class PerformanceConfig {

    private final SlowQueryInterceptor slowQueryInterceptor;
    private final DataSource dataSource;

    @Bean
    @ConditionalOnProperty(name = "app.performance.monitoring.enabled", havingValue = "true")
    public PerformanceMonitoringService performanceMonitoringService() {
        return new PerformanceMonitoringService(slowQueryInterceptor, dataSource);
    }

    @Scheduled(fixedRate = 300000)
    public void logPerformanceMetrics() {
        try {
            long slowQueryCount = slowQueryInterceptor.getSlowQueryCount();
            long totalQueryCount = slowQueryInterceptor.getTotalQueryCount();
            double slowQueryPercentage = slowQueryInterceptor.getSlowQueryPercentage();

            log.info("Performance Metrics - Total Queries: {}, Slow Queries: {}, Slow Query %: {:.2f}%",
                    totalQueryCount, slowQueryCount, slowQueryPercentage);

            checkDatabaseConnection();

        } catch (Exception e) {
            log.error("Error logging performance metrics", e);
        }
    }

    private void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                log.debug("Database connection is healthy");
            } else {
                log.warn("Database connection validation failed");
            }
        } catch (SQLException e) {
            log.error("Database connection check failed", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetPerformanceCounters() {
        slowQueryInterceptor.resetCounters();
        log.info("Performance counters reset");
    }

    public static class PerformanceMonitoringService {
        private final SlowQueryInterceptor slowQueryInterceptor;
        private final DataSource dataSource;

        public PerformanceMonitoringService(SlowQueryInterceptor slowQueryInterceptor, DataSource dataSource) {
            this.slowQueryInterceptor = slowQueryInterceptor;
            this.dataSource = dataSource;
        }

        public void recordQueryExecution(long executionTimeMs, String sql) {
            slowQueryInterceptor.recordSlowQuery(executionTimeMs, sql);
        }

        public PerformanceMetrics getCurrentMetrics() {
            return PerformanceMetrics.builder()
                    .slowQueryCount(slowQueryInterceptor.getSlowQueryCount())
                    .totalQueryCount(slowQueryInterceptor.getTotalQueryCount())
                    .slowQueryPercentage(slowQueryInterceptor.getSlowQueryPercentage())
                    .build();
        }
    }

    public static class PerformanceMetrics {
        private final long slowQueryCount;
        private final long totalQueryCount;
        private final double slowQueryPercentage;

        public PerformanceMetrics(long slowQueryCount, long totalQueryCount, double slowQueryPercentage) {
            this.slowQueryCount = slowQueryCount;
            this.totalQueryCount = totalQueryCount;
            this.slowQueryPercentage = slowQueryPercentage;
        }

        public long getSlowQueryCount() { return slowQueryCount; }
        public long getTotalQueryCount() { return totalQueryCount; }
        public double getSlowQueryPercentage() { return slowQueryPercentage; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long slowQueryCount;
            private long totalQueryCount;
            private double slowQueryPercentage;

            public Builder slowQueryCount(long slowQueryCount) {
                this.slowQueryCount = slowQueryCount;
                return this;
            }

            public Builder totalQueryCount(long totalQueryCount) {
                this.totalQueryCount = totalQueryCount;
                return this;
            }

            public Builder slowQueryPercentage(double slowQueryPercentage) {
                this.slowQueryPercentage = slowQueryPercentage;
                return this;
            }

            public PerformanceMetrics build() {
                return new PerformanceMetrics(slowQueryCount, totalQueryCount, slowQueryPercentage);
            }
        }
    }
}