package com.antock.api.member.application.service;

import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.domain.Member;

public interface MemberCacheService {
    MemberResponse getMemberFromCache(Long memberId);
    void cacheMember(Member member);
    void cacheMemberResponse(MemberResponse memberResponse);
    MemberResponse getMemberProfileFromCache(Long memberId);
    void cacheMemberProfile(MemberResponse memberResponse);
    void evictMemberCache(Long memberId);
    void evictAllMemberCache();
    CacheStatistics getCacheStatistics();

    class CacheStatistics {
        private final long cacheHits;
        private final long cacheMisses;
        private final long cacheErrors;
        private final double hitRate;
        private final long totalRequests;
        private final boolean cacheAvailable;

        public CacheStatistics(long cacheHits, long cacheMisses, long cacheErrors,
                               double hitRate, long totalRequests, boolean cacheAvailable) {
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.cacheErrors = cacheErrors;
            this.hitRate = hitRate;
            this.totalRequests = totalRequests;
            this.cacheAvailable = cacheAvailable;
        }

        public static CacheStatistics empty() {
            return new CacheStatistics(0, 0, 0, 0.0, 0, false);
        }

        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        public long getCacheErrors() { return cacheErrors; }
        public double getHitRate() { return hitRate; }
        public long getTotalRequests() { return totalRequests; }
        public boolean isCacheAvailable() { return cacheAvailable; }

        @Override
        public String toString() {
            return "CacheStatistics{" +
                    "cacheHits=" + cacheHits +
                    ", cacheMisses=" + cacheMisses +
                    ", cacheErrors=" + cacheErrors +
                    ", hitRate=" + hitRate +
                    ", totalRequests=" + totalRequests +
                    ", cacheAvailable=" + cacheAvailable +
                    '}';
        }
    }
}