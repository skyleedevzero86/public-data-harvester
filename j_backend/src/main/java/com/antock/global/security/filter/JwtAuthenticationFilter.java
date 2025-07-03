package com.antock.global.security.filter;

import com.antock.global.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("JWT 필터 처리 시작 - URI: {}", requestURI);

        String token = resolveToken(request);
        log.debug("토큰 추출 결과 - URI: {}, Token exists: {}", requestURI, token != null);

        if (token != null) {
            boolean isValid = jwtTokenProvider.validateToken(token);
            log.debug("토큰 검증 결과 - URI: {}, Valid: {}", requestURI, isValid);

            if (isValid) {
                try {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("JWT 인증 성공 - URI: {}, User: {}, Authorities: {}",
                            requestURI, auth.getName(), auth.getAuthorities());
                } catch (Exception e) {
                    log.error("JWT 인증 처리 중 오류 발생 - URI: {}, Error: {}", requestURI, e.getMessage(), e);
                    SecurityContextHolder.clearContext();
                }
            }
        } else {
            log.warn("JWT 토큰이 없음 - URI: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            log.debug("Authorization 헤더에서 토큰 발견 - URI: {}", request.getRequestURI());
            return bearerToken.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        log.debug("쿠키 확인 - URI: {}, Cookies exist: {}", request.getRequestURI(), cookies != null);

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.debug("쿠키 검사 - Name: {}, Value length: {}",
                        cookie.getName(), cookie.getValue() != null ? cookie.getValue().length() : 0);

                if ("accessToken".equals(cookie.getName())) {
                    String tokenValue = cookie.getValue();
                    log.info("쿠키에서 accessToken 발견 - URI: {}, Token length: {}",
                            request.getRequestURI(), tokenValue != null ? tokenValue.length() : 0);
                    return tokenValue;
                }
            }
        }

        log.warn("토큰을 찾을 수 없음 - URI: {}", request.getRequestURI());
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/WEB-INF/views/") ||
                path.startsWith("/.well-known/") ||
                path.equals("/members/join") ||
                path.equals("/members/login") ||
                path.startsWith("/api/v1/members/join") ||
                path.startsWith("/api/v1/members/login") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/static/") ||
                path.startsWith("/assets/") ||
                path.equals("/favicon.ico") ||
                path.equals("/robots.txt") ||
                path.startsWith("/h2-console/") ||
                path.startsWith("/coseller/save") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.equals("/actuator/health") ||
                path.equals("/") ||
                path.equals("/home") ||
                path.equals("/index") ||
                path.startsWith("/debug/") ||
                path.equals("/error");
    }
}