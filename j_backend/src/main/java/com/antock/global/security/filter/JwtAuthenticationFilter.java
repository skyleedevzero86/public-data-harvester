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
            try {
                boolean isValid = jwtTokenProvider.validateToken(token);
                log.debug("토큰 검증 결과 - URI: {}, Valid: {}", requestURI, isValid);

                if (isValid) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("JWT 인증 성공 - URI: {}, User: {}, Authorities: {}",
                            requestURI, auth.getName(), auth.getAuthorities());
                } else {
                    log.warn("유효하지 않은 토큰 - URI: {}", requestURI);
                    clearTokenCookie(response);
                }
            } catch (Exception e) {
                log.error("JWT 인증 처리 중 오류 발생 - URI: {}, Error: {}", requestURI, e.getMessage());
                SecurityContextHolder.clearContext();
                clearTokenCookie(response);
            }
        } else {
            log.debug("JWT 토큰이 없음 - URI: {}", requestURI);
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
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    String tokenValue = cookie.getValue();
                    log.debug("쿠키에서 accessToken 발견 - URI: {}, Token length: {}",
                            request.getRequestURI(), tokenValue != null ? tokenValue.length() : 0);
                    return tokenValue;
                }
            }
        }

        return null;
    }

    private void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        log.info("유효하지 않은 토큰 쿠키 삭제 완료");
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