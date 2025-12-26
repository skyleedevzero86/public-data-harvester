package com.antock.global.config;

import com.antock.api.member.application.service.RateLimitServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupLogger {

    private final Environment environment;
    private final RateLimitServiceInterface rateLimitService;

    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profileInfo = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";

        log.info("======================================");
        log.info("Antock 시스템이 성공적으로 시작되었습니다!");
        log.info("======================================");
        log.info("활성 프로필: {}", profileInfo);
        log.info("서버 포트: {}", environment.getProperty("server.port", "8080"));
        log.info("데이터베이스: {}", getDatabaseInfo());
        log.info("속도 제한: {}", getRateLimitInfo());
        log.info("헬스 체크: http://localhost:{}/actuator/health",
                environment.getProperty("server.port", "8080"));

        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            log.info("H2 콘솔: http://localhost:{}/h2-console", environment.getProperty("server.port", "8080"));
        }

        log.info("======================================");
    }

    private String getDatabaseInfo() {
        String url = environment.getProperty("spring.datasource.url", "Unknown");
        if (url.contains("postgresql")) {
            return "PostgreSQL";
        } else if (url.contains("h2")) {
            return "H2 (In-Memory)";
        }
        return "Unknown";
    }

    private String getRateLimitInfo() {
        if (rateLimitService.isRedisAvailable()) {
            return "Redis-based";
        } else {
            return "Memory-based";
        }
    }
}