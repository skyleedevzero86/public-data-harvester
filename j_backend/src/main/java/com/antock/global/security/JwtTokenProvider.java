package com.antock.global.security;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final String issuer;
    private final String audience;
    private final String algorithm;
    private final boolean keyRotationEnabled;
    private final int keyRotationIntervalHours;
    private final UserDetailsService userDetailsService;

    private final Map<String, SecretKey> rotatedKeys = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private LocalDateTime lastKeyRotation;

    public JwtTokenProvider(
            @Value("${custom.jwt.secretKey}") String secretKeyString,
            @Value("${custom.jwt.accessTokenExpirationSeconds}") long accessTokenValidityInSeconds,
            @Value("${custom.jwt.refreshTokenExpirationSeconds}") long refreshTokenValidityInSeconds,
            @Value("${custom.jwt.issuer}") String issuer,
            @Value("${custom.jwt.audience}") String audience,
            @Value("${custom.jwt.algorithm}") String algorithm,
            @Value("${custom.jwt.keyRotationEnabled}") boolean keyRotationEnabled,
            @Value("${custom.jwt.keyRotationIntervalHours}") int keyRotationIntervalHours,
            UserDetailsService userDetailsService) {

        this.secretKey = generateSecretKey(secretKeyString);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        this.issuer = issuer;
        this.audience = audience;
        this.algorithm = algorithm;
        this.keyRotationEnabled = keyRotationEnabled;
        this.keyRotationIntervalHours = keyRotationIntervalHours;
        this.userDetailsService = userDetailsService;
        this.lastKeyRotation = LocalDateTime.now();

        log.info("JWT Token Provider initialized with algorithm: {}, issuer: {}, audience: {}",
                algorithm, issuer, audience);
    }

    private SecretKey generateSecretKey(String secretKeyString) {
        if (secretKeyString == null || secretKeyString.trim().isEmpty() ||
                "your-very-long-secret-key-that-should-be-at-least-64-characters-long-for-hs512-algorithm-security".equals(secretKeyString)) {
            log.warn("Using default JWT secret key. Please set JWT_SECRET_KEY environment variable in production!");
            byte[] keyBytes = new byte[64];
            secureRandom.nextBytes(keyBytes);
            return Keys.hmacShaKeyFor(keyBytes);
        }

        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.warn("JWT secret key is too short. Recommended minimum length is 32 bytes.");
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);

            byte[] remainingBytes = new byte[paddedKey.length - keyBytes.length];
            secureRandom.nextBytes(remainingBytes);
            System.arraycopy(remainingBytes, 0, paddedKey, keyBytes.length, remainingBytes.length);

            return Keys.hmacShaKeyFor(paddedKey);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(String username, String role) {
        return createToken(username, role, accessTokenValidityInMilliseconds, "ACCESS");
    }

    public String createRefreshToken(String username, String role) {
        return createToken(username, role, refreshTokenValidityInMilliseconds, "REFRESH");
    }

    private String createToken(String username, String role, long validityInMilliseconds, String tokenType) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", tokenType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(validity)
                .setNotBefore(now)
                .signWith(secretKey, SignatureAlgorithm.forName(algorithm))
                .compact();
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = parseClaims(token);
            String username = claims.getSubject();
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to create authentication from token: {}", e.getMessage());
            return null;
        }
    }

    public Claims parseClaims(String token) {
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
            log.warn("Invalid JWT token format: {}", e.getMessage());
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
            Claims claims = parseClaims(token);

            if (claims.getExpiration().before(new Date())) {
                log.debug("Token has expired");
                return false;
            }

            if (claims.getNotBefore() != null && claims.getNotBefore().after(new Date())) {
                log.debug("Token is not yet valid");
                return false;
            }

            if (!issuer.equals(claims.getIssuer())) {
                log.warn("Invalid JWT issuer: expected {}, got {}", issuer, claims.getIssuer());
                return false;
            }

            if (!audience.equals(claims.getAudience())) {
                log.warn("Invalid JWT audience: expected {}, got {}", audience, claims.getAudience());
                return false;
            }

            return true;

        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Failed to extract expiration date from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isTokenNearExpiration(String token, long thresholdMinutes) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            if (expiration == null) {
                return true;
            }

            long currentTime = System.currentTimeMillis();
            long expirationTime = expiration.getTime();
            long timeUntilExpiration = expirationTime - currentTime;
            long thresholdMillis = thresholdMinutes * 60 * 1000;

            return timeUntilExpiration <= thresholdMillis;
        } catch (Exception e) {
            return true;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = parseClaims(token);
            return (String) claims.get("type");
        } catch (Exception e) {
            log.error("Failed to extract token type from token: {}", e.getMessage());
            return null;
        }
    }

    public void rotateSecretKey() {
        if (!keyRotationEnabled) {
            log.info("Key rotation is disabled");
            return;
        }

        String currentKeyId = String.valueOf(System.currentTimeMillis());
        SecretKey newKey = generateSecretKey(null);
        rotatedKeys.put(currentKeyId, newKey);
        lastKeyRotation = LocalDateTime.now();

        log.info("Secret key rotated successfully with ID: {}", currentKeyId);
    }

    public Map<String, Object> getTokenInfo(String token) {
        try {
            Claims claims = parseClaims(token);
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("subject", claims.getSubject());
            tokenInfo.put("issuer", claims.getIssuer());
            tokenInfo.put("audience", claims.getAudience());
            tokenInfo.put("issuedAt", claims.getIssuedAt());
            tokenInfo.put("expiration", claims.getExpiration());
            tokenInfo.put("type", claims.get("type"));
            tokenInfo.put("role", claims.get("role"));
            return tokenInfo;
        } catch (Exception e) {
            log.error("Failed to extract token info: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}