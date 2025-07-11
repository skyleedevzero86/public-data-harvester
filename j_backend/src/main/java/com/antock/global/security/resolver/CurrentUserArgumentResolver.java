package com.antock.global.security.resolver;

import com.antock.api.member.application.service.AuthTokenService;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthTokenService authTokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
                parameter.getParameterType().equals(AuthenticatedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("SecurityContext에서 인증 정보 확인 - principal: {}", authentication.getName());

            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (request != null) {
                String token = extractTokenFromCookies(request);
                if (token != null) {
                    try {
                        Claims claims = authTokenService.parseToken(token);

                        AuthenticatedUser user = AuthenticatedUser.builder()
                                .id(claims.get("id", Long.class))
                                .username(claims.getSubject())
                                .nickname((String) claims.get("nickname"))
                                .role((String) claims.get("role"))
                                .build();

                        log.debug("CurrentUser 생성 완료 - id: {}, username: {}", user.getId(), user.getUsername());
                        return user;

                    } catch (Exception e) {
                        log.error("토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
                    }
                }
            }
        }

        log.warn("CurrentUser 정보를 생성할 수 없음 - 인증되지 않은 요청");
        return null;
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}