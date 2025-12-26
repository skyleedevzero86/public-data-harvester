package com.antock.api.member.application.service.strategy;

import com.antock.api.member.application.service.RateLimitSecurityInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimitStrategy implements RateLimitStrategy {

    @Autowired(required = false)
    @Qualifier("customStringRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${custom.redis.rate-limit-key-prefix:rate_limit:}")
    private String keyPrefix;

    private static final String RATE_LIMIT_SCRIPT = "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = redis.call('GET', key) " +
            "if current == false then " +
            "  redis.call('SETEX', key, window, '1') " +
            "  return 1 " +
            "end " +
            "local count = tonumber(current) " +
            "if count >= limit then " +
            "  return -1 " +
            "end " +
            "redis.call('INCR', key) " +
            "return count + 1";

    private static final String BLOCK_SCRIPT = "local blockKey = KEYS[1] " +
            "local reason = ARGV[1] " +
            "local expiry = ARGV[2] " +
            "redis.call('SETEX', blockKey, expiry, reason) " +
            "return 1";

    private static final String UNBLOCK_SCRIPT = "local blockKey = KEYS[1] " +
            "redis.call('DEL', blockKey) " +
            "return 1";

    @Override
    public void checkRateLimit(String identifier, String action, int requestsPerMinute) {
        String key = generateKey(identifier, action);
        RedisScript<Long> script = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);

        List<String> keys = Arrays.asList(key);
        List<String> args = Arrays.asList(String.valueOf(requestsPerMinute), "60");

        Long result = redisTemplate.execute(script, keys, args.toArray());
        if (result == null || result < 0) {
            throw new SecurityException("Rate limit exceeded for identifier: " + identifier);
        }
    }

    @Override
    public int getCurrentCount(String identifier, String action) {
        String key = generateKey(identifier, action);
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    @Override
    public void resetLimit(String identifier, String action) {
        String key = generateKey(identifier, action);
        redisTemplate.delete(key);
        log.info("Rate limit reset for identifier: {} action: {} (Redis)", identifier, action);
    }

    @Override
    public void blockIdentifier(String identifier, String reason, long blockDurationMinutes) {
        String blockKey = keyPrefix + "block:" + identifier;
        RedisScript<Long> script = new DefaultRedisScript<>(BLOCK_SCRIPT, Long.class);

        List<String> keys = Arrays.asList(blockKey);
        List<String> args = Arrays.asList(reason, String.valueOf(blockDurationMinutes * 60));
        redisTemplate.execute(script, keys, args.toArray());
        log.info("식별자 {}가 Redis에서 차단됨", identifier);
    }

    @Override
    public void unblockIdentifier(String identifier) {
        String blockKey = keyPrefix + "block:" + identifier;
        RedisScript<Long> script = new DefaultRedisScript<>(UNBLOCK_SCRIPT, Long.class);

        List<String> keys = Arrays.asList(blockKey);
        List<String> args = Arrays.asList();
        redisTemplate.execute(script, keys, args.toArray());
        log.info("식별자 {}의 차단이 Redis에서 해제됨", identifier);
    }

    @Override
    public boolean isIdentifierBlocked(String identifier) {
        String blockKey = keyPrefix + "block:" + identifier;
        String blockInfo = redisTemplate.opsForValue().get(blockKey);
        return blockInfo != null;
    }

    @Override
    public void addToWhitelist(String identifier) {
        String whitelistKey = keyPrefix + "whitelist:" + identifier;
        redisTemplate.opsForValue().set(whitelistKey, "1");
        log.info("식별자 {}가 Redis 화이트리스트에 추가됨", identifier);
    }

    @Override
    public void removeFromWhitelist(String identifier) {
        String whitelistKey = keyPrefix + "whitelist:" + identifier;
        redisTemplate.delete(whitelistKey);
        log.info("식별자 {}가 Redis 화이트리스트에서 제거됨", identifier);
    }

    @Override
    public boolean isWhitelisted(String identifier) {
        String whitelistKey = keyPrefix + "whitelist:" + identifier;
        String result = redisTemplate.opsForValue().get(whitelistKey);
        return result != null;
    }

    @Override
    public RateLimitSecurityInfo getSecurityInfo(String identifier) {
        String blockKey = keyPrefix + "block:" + identifier;
        String whitelistKey = keyPrefix + "whitelist:" + identifier;

        String blockReason = redisTemplate.opsForValue().get(blockKey);
        String whitelistStatus = redisTemplate.opsForValue().get(whitelistKey);

        boolean isBlocked = blockReason != null;
        boolean isWhitelisted = whitelistStatus != null;

        return new RateLimitSecurityInfo(isBlocked, blockReason, 0, isWhitelisted, 0, 0);
    }

    @Override
    public boolean isAvailable() {
        try {
            if (redisConnectionFactory != null && redisTemplate != null) {
                try (var connection = redisConnectionFactory.getConnection()) {
                    connection.ping();
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("Redis 연결 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    private String generateKey(String identifier, String action) {
        return keyPrefix + identifier + ":" + action;
    }
}

