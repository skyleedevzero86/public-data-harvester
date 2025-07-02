package com.antock.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class RedisHealthChecker {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisHealthChecker(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkRedisConnection() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok");
            String result = redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");

            if ("ok".equals(result)) {
                log.info("Redis 연결 성공");
            } else {
                log.warn("Redis 연결 테스트 실패");
            }
        } catch (Exception e) {
            log.error("Redis 연결 실패: {}", e.getMessage());
            log.info("docker run -d -p 6379:6379 redis:7-alpine' 명령어로 Redis를 시작하세요");
        }
    }
}