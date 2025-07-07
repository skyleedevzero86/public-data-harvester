package com.antock.global.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Unauthorized error: {}", authException.getMessage());
        log.error("Request URI: {}", request.getRequestURI());
        log.error("Request Method: {}", request.getMethod());

        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");

        boolean isApiRequest = requestURI.startsWith("/api/") ||
                (acceptHeader != null && acceptHeader.contains("application/json"))||
                requestURI.startsWith("/coseller/");

        if (isApiRequest) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            String jsonResponse = """
                {
                    "resultCode": 401,
                    "resultMsg": "인증이 필요합니다.",
                    "data": null
                }
                """;
            response.getWriter().write(jsonResponse);
        } else {
            response.sendRedirect("/members/login");
        }
    }
}