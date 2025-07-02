package com.antock.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Configuration
@Profile("dev")
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(name = "spring.data.redis.host")
@Import({
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
})
public class RedisAutoConfiguration {

    public RedisAutoConfiguration() {
        log.info("개발 프로필에 Redis 자동 구성이 활성화되었습니다.");
    }
}