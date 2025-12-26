package com.antock.api.member.application.service.strategy;

import com.antock.api.member.application.service.RateLimitSecurityInfo;

public interface RateLimitStrategy {

    void checkRateLimit(String identifier, String action, int requestsPerMinute);

    int getCurrentCount(String identifier, String action);

    void resetLimit(String identifier, String action);

    void blockIdentifier(String identifier, String reason, long blockDurationMinutes);

    void unblockIdentifier(String identifier);

    boolean isIdentifierBlocked(String identifier);

    void addToWhitelist(String identifier);

    void removeFromWhitelist(String identifier);

    boolean isWhitelisted(String identifier);

    RateLimitSecurityInfo getSecurityInfo(String identifier);

    boolean isAvailable();
}

