package com.antock.global.security.filter;

import com.antock.global.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService,
                                   ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("Processing request: {} {}", request.getMethod(), requestURI);

        try {
            String token = resolveToken(request);
            log.debug("Extracted token: {}", token != null ? "***" + token.substring(Math.max(0, token.length() - 10)) : "null");

            if (StringUtils.hasText(token)) {
                if (jwtTokenProvider.validateToken(token)) {
                    String tokenType = jwtTokenProvider.getTokenType(token);
                    log.debug("Token type: {}", tokenType);

                    if ("ACCESS".equals(tokenType)) {
                        Authentication authentication = getAuthentication(token);

                        if (authentication != null && authentication.isAuthenticated()) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Authentication set for user: {}", authentication.getName());
                        } else {
                            log.warn("Failed to create authentication from token");
                        }
                    } else {
                        log.warn("Invalid token type: {}. Expected 'ACCESS' token", tokenType);
                        clearTokenCookie(response);
                    }
                } else {
                    log.warn("Invalid JWT token provided for request: {}", requestURI);
                    clearTokenCookie(response);
                }
            } else {
                log.debug("No token found for request: {}", requestURI);
            }

        } catch (Exception e) {
            log.error("JWT token processing error for request {}: {}", requestURI, e.getMessage(), e);
            SecurityContextHolder.clearContext();
            clearTokenCookie(response);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("Found Authorization header token");
            return bearerToken.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> tokenCookie = Arrays.stream(cookies)
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .findFirst();

            if (tokenCookie.isPresent()) {
                String token = tokenCookie.get().getValue();
                if (StringUtils.hasText(token)) {
                    log.debug("Found accessToken cookie");
                    return token;
                }
            }
        }

        log.debug("No token found in request");
        return null;
    }

    private Authentication getAuthentication(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            if (username != null) {
                log.debug("Creating authentication for username: {}", username);
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

    private void clearTokenCookie(HttpServletResponse response) {
        log.debug("Clearing token cookies");
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        response.addCookie(refreshTokenCookie);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        boolean shouldSkip = path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/api/v1/members/join") ||
                path.equals("/api/v1/members/login") ||
                path.startsWith("/actuator/") ||
                path.equals("/favicon.ico") ||
                path.equals("/") ||
                path.equals("/members/login") ||
                path.equals("/members/join");

        if (shouldSkip) {
            log.debug("Skipping JWT filter for path: {}", path);
        }

        return shouldSkip;
    }
}