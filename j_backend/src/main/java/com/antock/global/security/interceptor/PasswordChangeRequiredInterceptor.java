package com.antock.global.security.interceptor;

import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.global.security.dto.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeRequiredInterceptor implements HandlerInterceptor {

    private final MemberApplicationService memberApplicationService;

    private final List<String> excludePatterns = Arrays.asList(
            "/members/password/change",
            "/members/logout",
            "/api/v1/members/me/password",
            "/api/v1/members/me/password/status",
            "/static",
            "/css",
            "/js",
            "/images"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        if (excludePatterns.stream().anyMatch(requestURI::startsWith)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        try {
            AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();


            if (memberApplicationService.isPasswordChangeRequired(user.getId())) {
                log.info("비밀번호 변경 필요 - 사용자를 비밀번호 변경 페이지로 리다이렉트: memberId={}", user.getId());


                if (requestURI.startsWith("/api/")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":\"MEMBER_4006\",\"message\":\"비밀번호 변경이 필요합니다.\"}");
                    return false;
                }

                response.sendRedirect("/members/password/change");
                return false;
            }

        } catch (Exception e) {
            log.error("비밀번호 변경 필요 여부 확인 중 오류 발생", e);

            return true;
        }

        return true;
    }
}