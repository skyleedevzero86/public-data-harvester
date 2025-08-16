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

        try {
            String token = resolveToken(request);

            if (StringUtils.hasText(token)) {

                if (jwtTokenProvider.validateToken(token)) {

                    String tokenType = jwtTokenProvider.getTokenType(token);
                    if ("access".equals(tokenType)) {
                        Authentication authentication = jwtTokenProvider.getAuthentication(token);

                        if (authentication != null && authentication.isAuthenticated()) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            if (jwtTokenProvider.isTokenNearExpiration(token, 15)) {
                                String username = jwtTokenProvider.getUsernameFromToken(token);
                                log.warn("Access token for user '{}' will expire soon", username);
                            }
                        }
                    } else {
                        log.warn("Invalid token type: {}. Expected 'access' token", tokenType);
                    }
                } else {
                    log.warn("Invalid JWT token provided");
                    clearTokenCookie(response);
                }
            }

        } catch (Exception e) {
            log.error("JWT token processing error: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            clearTokenCookie(response);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> tokenCookie = Arrays.stream(cookies)
                    .filter(cookie -> "access_token".equals(cookie.getName()))
                    .findFirst();

            if (tokenCookie.isPresent()) {
                String token = tokenCookie.get().getValue();
                if (StringUtils.hasText(token)) {
                    return token;
                }
            }
        }

        return null;
    }

    private void clearTokenCookie(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.startsWith("/static/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.equals("/api/v1/members/join") ||
                path.equals("/api/v1/members/login") ||
                path.startsWith("/actuator/") ||
                path.equals("/favicon.ico");
    }
}