package com.antock.api.member.application.service;

import com.antock.api.member.domain.Member;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.antock.global.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthTokenService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
        log.info("AuthTokenService initialized with JwtTokenProvider");
    }

    public String generateAccessToken(Member member) {
        return jwtTokenProvider.createAccessToken(member.getUsername(), member.getRole().name());
    }

    public String generateRefreshToken(Member member) {
        return jwtTokenProvider.createRefreshToken(member.getUsername(), member.getRole().name());
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    public Long getMemberIdFromToken(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            if (username != null) {
                return null;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to extract member ID from token: {}", e.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    public String getRoleFromToken(String token) {
        try {
            return (String) jwtTokenProvider.parseClaims(token).get("role");
        } catch (Exception e) {
            log.error("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }

    public String getTokenType(String token) {
        return jwtTokenProvider.getTokenType(token);
    }
}
