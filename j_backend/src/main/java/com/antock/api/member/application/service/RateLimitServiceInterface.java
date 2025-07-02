package com.antock.api.member.application.service;

public interface RateLimitServiceInterface {
    void checkRateLimit(String identifier, String action);
    int getCurrentCount(String identifier, String action);
    void resetLimit(String identifier, String action);
    boolean isRedisAvailable();
}