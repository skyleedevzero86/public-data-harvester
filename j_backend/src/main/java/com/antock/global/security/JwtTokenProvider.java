package com.antock.global.security;

import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

        SecretKey currentKey = getCurrentSecretKey();

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("type", tokenType);
        claims.put("keyId", getCurrentKeyId());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(validity)
                .setNotBefore(now)
                .setId(generateTokenId())
                .signWith(currentKey, SignatureAlgorithm.forName(algorithm))
                .compact();
    }

    private SecretKey getCurrentSecretKey() {
        if (!keyRotationEnabled) {
            return secretKey;
        }

        String currentKeyId = getCurrentKeyId();
        return rotatedKeys.computeIfAbsent(currentKeyId, k -> {
            log.info("Generating new rotated key with ID: {}", currentKeyId);
            return generateSecretKey(null);
        });
    }

    private String getCurrentKeyId() {
        if (!keyRotationEnabled) {
            return "default";
        }

        LocalDateTime now = LocalDateTime.now();
        if (lastKeyRotation.plusHours(keyRotationIntervalHours).isBefore(now)) {
            lastKeyRotation = now;
            cleanupOldKeys();
        }

        return String.format("key_%d", lastKeyRotation.getHour() / keyRotationIntervalHours);
    }

    private void cleanupOldKeys() {
        int maxKeysToKeep = 3;
        if (rotatedKeys.size() > maxKeysToKeep) {
            rotatedKeys.entrySet().removeIf(entry -> {
                String keyId = entry.getKey();
                if (!keyId.equals(getCurrentKeyId())) {
                    log.debug("Removing old rotated key: {}", keyId);
                    return true;
                }
                return false;
            });
        }
    }

    private String generateTokenId() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = parseClaims(token);
            String username = claims.getSubject();

            if (username == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

        } catch (JwtException e) {
            log.warn("JWT token parsing failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Claims parseClaims(String token) {
        try {
            SecretKey key = getSecretKeyFromToken(token);
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (SecurityException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private SecretKey getSecretKeyFromToken(String token) {
        try {
            String[] chunks = token.split("\\.");
            if (chunks.length < 2) {
                return secretKey;
            }

            String headerJson = new String(java.util.Base64.getUrlDecoder().decode(chunks[0]));

            if (headerJson.contains("\"kid\"")) {
                String kidPattern = "\"kid\"\\s*:\\s*\"([^\"]+)\"";
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(kidPattern);
                java.util.regex.Matcher matcher = pattern.matcher(headerJson);
                if (matcher.find()) {
                    String keyId = matcher.group(1);
                    if (rotatedKeys.containsKey(keyId)) {
                        return rotatedKeys.get(keyId);
                    }
                }
            }

            return secretKey;

        } catch (Exception e) {
            log.debug("Could not extract key ID from token, using default key");
            return secretKey;
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);

            if (claims.getExpiration().before(new Date())) {
                return false;
            }

            if (claims.getNotBefore().after(new Date())) {
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
            log.warn("Could not extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.warn("Could not extract expiration date from token: {}", e.getMessage());
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
                return false;
            }

            long thresholdMs = thresholdMinutes * 60 * 1000;
            return (expiration.getTime() - System.currentTimeMillis()) <= thresholdMs;

        } catch (Exception e) {
            return false;
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = parseClaims(token);
            return (String) claims.get("type");
        } catch (Exception e) {
            return null;
        }
    }

    public void rotateSecretKey() {
        if (!keyRotationEnabled) {
            log.info("Key rotation is disabled");
            return;
        }

        String newKeyId = getCurrentKeyId();
        SecretKey newKey = generateSecretKey(null);
        rotatedKeys.put(newKeyId, newKey);

        log.info("Secret key rotated successfully. New key ID: {}", newKeyId);
    }

    public Map<String, Object> getTokenInfo(String token) {
        try {
            Claims claims = parseClaims(token);
            Map<String, Object> info = new HashMap<>();

            info.put("subject", claims.getSubject());
            info.put("issuer", claims.getIssuer());
            info.put("audience", claims.getAudience());
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiration", claims.getExpiration());
            info.put("notBefore", claims.getNotBefore());
            info.put("id", claims.getId());
            info.put("role", claims.get("role"));
            info.put("type", claims.get("type"));
            info.put("keyId", claims.get("keyId"));

            return info;

        } catch (Exception e) {
            log.warn("Could not extract token info: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}