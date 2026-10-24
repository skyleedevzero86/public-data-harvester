package com.antock.global.security.handler;

import com.antock.global.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        log.warn("권한이 없는 요청: {} {}", request.getMethod(), requestURI);

        boolean registration = requestURI.startsWith("/coseller/save");
        if (requestURI.startsWith("/api/") || registration) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            String message = registration
                    ? "관리자 또는 매니저만 등록할 수 있습니다."
                    : "Access denied to this resource";
            ApiResponse<Object> apiResponse = ApiResponse.error(HttpStatus.FORBIDDEN, message);

            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        } else {
            response.sendRedirect("/");
        }
    }
}
