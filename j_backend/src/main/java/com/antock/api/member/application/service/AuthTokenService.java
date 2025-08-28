package com.antock.api.member.application.service;

import com.antock.api.member.domain.Member;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthTokenService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public AuthTokenService(
            @Value("${custom.jwt.secretKey}") String secretKeyString,
            @Value("${custom.jwt.accessTokenExpirationSeconds}") long accessTokenExpirationSeconds,
            @Value("${custom.jwt.refreshTokenExpirationSeconds}") long refreshTokenExpirationSeconds) {

        if (secretKeyString == null || secretKeyString.trim().isEmpty() ||
                secretKeyString.length() < 32) {
            log.error("JWT secret key is too short or empty. Minimum length required: 32 characters");
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters long");
        }

        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;

        log.info("AuthTokenService initialized with secure key length: {} bytes", secretKeyString.length());
    }

    public String generateAccessToken(Member member) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationSeconds * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("username", member.getUsername());
        claims.put("role", member.getRole().name());
        claims.put("type", "ACCESS");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(member.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Member member) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationSeconds * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("username", member.getUsername());
        claims.put("type", "REFRESH");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(member.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty or invalid: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);

            String tokenType = claims.get("type", String.class);
            if (tokenType == null || !"ACCESS".equals(tokenType)) {
                log.warn("Invalid token type: {}", tokenType);
                return false;
            }

            if (claims.getExpiration().before(new Date())) {
                log.warn("Token has expired");
                return false;
            }

            return true;
        } catch (BusinessException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getMemberIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("id", Long.class);
        } catch (Exception e) {
            log.error("Failed to extract member ID from token: {}", e.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.error("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = parseToken(token);
            return (String) claims.get("type");
        } catch (Exception e) {
            log.error("Failed to extract token type from token: {}", e.getMessage());
            return null;
        }
    }
}