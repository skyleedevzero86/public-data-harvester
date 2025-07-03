package com.antock.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(@Value("${custom.jwt.secretKey}") String secretKeyString,
                            @Value("${custom.jwt.accessTokenExpirationSeconds}") long validityInSeconds,
                            UserDetailsService userDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.validityInMilliseconds = validityInSeconds * 1000;
        this.userDetailsService = userDetailsService;

        log.info("JwtTokenProvider 초기화 완료 - secretKey length: {}, validity: {}초",
                secretKeyString.length(), validityInSeconds);
    }

    public String createToken(String username, String role) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);
        claims.put("type", "ACCESS");

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        log.debug("토큰 생성 완료 - username: {}, role: {}, length: {}", username, role, token.length());
        return token;
    }

    public Authentication getAuthentication(String token) {
        try {
            log.debug("토큰에서 인증 정보 추출 시작 - token length: {}", token.length());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String role = (String) claims.get("role");
            String type = (String) claims.get("type");

            log.debug("토큰 클레임 추출 완료 - username: {}, role: {}, type: {}", username, role, type);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            log.info("인증 객체 생성 완료 - username: {}, authorities: {}", username, authorities);

            return auth;

        } catch (Exception e) {
            log.error("토큰에서 인증 정보 추출 실패 - error: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            log.debug("토큰 검증 시작 - token length: {}", token.length());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            boolean isValid = expiration.after(new Date());

            log.debug("토큰 검증 완료 - valid: {}, expiration: {}", isValid, expiration);
            return isValid;

        } catch (Exception e) {
            log.error("토큰 검증 실패 - error: {}", e.getMessage());
            return false;
        }
    }
}