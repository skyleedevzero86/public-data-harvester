package com.antock.web.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.AuthTokenService;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberWebController {

    private final MemberApplicationService memberApplicationService;
    private final AuthTokenService authTokenService;

    @GetMapping("/members/join")
    public String joinForm(Model model) {
        model.addAttribute("memberJoinRequest", new MemberJoinRequest());
        return "member/join";
    }

    @PostMapping("/members/join")
    public String join(@Valid @ModelAttribute MemberJoinRequest request,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       HttpServletRequest httpRequest) {

        log.info("회원가입 요청 받음: {}", request.getUsername());
        log.info("요청 데이터: username={}, nickname={}, email={}",
                request.getUsername(), request.getNickname(), request.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("유효성 검증 실패:");
            bindingResult.getAllErrors().forEach(error ->
                    log.warn("- {}", error.getDefaultMessage()));
            return "member/join";
        }

        try {
            log.info("회원가입 서비스 호출 시작");
            MemberResponse response = memberApplicationService.join(request);
            log.info("회원가입 성공: {}", response.getUsername());

            redirectAttributes.addFlashAttribute("successMessage",
                    "회원가입이 완료되었습니다. 관리자 승인 후 로그인이 가능합니다.");
            return "redirect:/members/login";
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage(), e);
            bindingResult.reject("joinFailed", e.getMessage());
            return "member/join";
        }
    }

    @GetMapping("/members/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {

            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setHttpOnly(true);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            refreshTokenCookie.setHttpOnly(true);
            response.addCookie(refreshTokenCookie);

            SecurityContextHolder.clearContext();

            log.info("웹 로그아웃 완료");
            redirectAttributes.addFlashAttribute("successMessage", "로그아웃되었습니다.");
            return "redirect:/members/login";

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "로그아웃 처리 중 오류가 발생했습니다.");
            return "redirect:/members/login";
        }
    }

    @GetMapping("/members/login")
    public String loginForm() {
        log.info("로그인 폼 페이지 요청");
        return "member/login";
    }

    @PostMapping("/members/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpServletResponse response,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        try {
            log.info("웹 로그인 시도: username={}", username);

            MemberLoginRequest loginRequest = MemberLoginRequest.builder()
                    .username(username)
                    .password(password)
                    .build();

            String clientIp = getClientIp(request);
            MemberLoginResponse loginResponse = memberApplicationService.login(loginRequest, clientIp);

            Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.getAccessToken());
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(3600);
            accessTokenCookie.setHttpOnly(true);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(86400);
            refreshTokenCookie.setHttpOnly(true);
            response.addCookie(refreshTokenCookie);

            log.info("웹 로그인 성공: username={}, accessToken length: {}",
                    username, loginResponse.getAccessToken().length());
            log.info("쿠키 설정 완료 - accessToken length: {}", loginResponse.getAccessToken().length());

            redirectAttributes.addFlashAttribute("successMessage", "로그인되었습니다.");
            return "redirect:/members/profile";

        } catch (Exception e) {
            log.error("웹 로그인 실패: username={}, error={}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/login";
        }
    }

    @GetMapping("/members/profile")
    public String profile(Model model, HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("인증되지 않은 사용자의 프로필 접근 시도");
                return "redirect:/members/login";
            }

            String username = authentication.getName();
            log.debug("프로필 페이지 접근 - username: {}", username);

            Long memberId = extractMemberIdFromToken(request);
            if (memberId == null) {
                log.warn("토큰에서 사용자 ID를 찾을 수 없음");
                return "redirect:/members/login";
            }

            MemberResponse member = memberApplicationService.getMemberInfo(memberId);
            model.addAttribute("member", member);

            // memberUpdateRequest 객체를 Model에 추가 (JSP form 바인딩용)
            MemberUpdateRequest memberUpdateRequest = MemberUpdateRequest.builder()
                    .nickname(member.getNickname())
                    .email(member.getEmail())
                    .build();
            model.addAttribute("memberUpdateRequest", memberUpdateRequest);

            return "member/profile";

        } catch (Exception e) {
            log.error("프로필 페이지 로드 중 오류: {}", e.getMessage(), e);
            return "redirect:/members/login";
        }
    }

    private Long extractMemberIdFromToken(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        return authTokenService.getMemberIdFromToken(token);
                    }
                }
            }
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    @PostMapping("/members/profile")
    public String updateProfile(HttpServletRequest request,
                                @Valid @ModelAttribute MemberUpdateRequest updateRequest,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (bindingResult.hasErrors()) {

            try {
                Long memberId = extractMemberIdFromToken(request);
                if (memberId != null) {
                    MemberResponse member = memberApplicationService.getMemberInfo(memberId);
                    model.addAttribute("member", member);
                }
            } catch (Exception e) {
                log.error("프로필 업데이트 에러 처리 중 오류: {}", e.getMessage());
                return "redirect:/members/login";
            }
            return "member/profile";
        }

        try {
            Long memberId = extractMemberIdFromToken(request);
            if (memberId == null) {
                log.warn("토큰에서 사용자 ID를 찾을 수 없음");
                return "redirect:/members/login";
            }

            memberApplicationService.updateProfile(memberId, updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "프로필이 수정되었습니다.");
            return "redirect:/members/profile";
        } catch (Exception e) {
            log.error("프로필 업데이트 실패: {}", e.getMessage());
            bindingResult.reject("updateFailed", e.getMessage());

            try {
                Long memberId = extractMemberIdFromToken(request);
                if (memberId != null) {
                    MemberResponse member = memberApplicationService.getMemberInfo(memberId);
                    model.addAttribute("member", member);
                }
            } catch (Exception ex) {
                log.error("프로필 업데이트 에러 처리 중 오류: {}", ex.getMessage());
                return "redirect:/members/login";
            }
            return "member/profile";
        }
    }

    @GetMapping("/members/admin/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String memberList(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<MemberResponse> members = memberApplicationService.getMembers(pageable);
        model.addAttribute("members", members);
        return "member/admin/list";
    }

    @GetMapping("/members/admin/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String pendingMembers(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<MemberResponse> pendingMembers = memberApplicationService.getPendingMembers(pageable);
        model.addAttribute("pendingMembers", pendingMembers);
        return "member/admin/pending";
    }

    @PostMapping("/members/admin/{memberId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String approveMember(@PathVariable Long memberId,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        try {
            Long approverId = extractMemberIdFromToken(request);
            if (approverId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "인증 정보를 찾을 수 없습니다.");
                return "redirect:/members/login";
            }

            memberApplicationService.approveMember(memberId, approverId);
            redirectAttributes.addFlashAttribute("successMessage", "회원이 승인되었습니다.");
        } catch (Exception e) {
            log.error("회원 승인 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/pending";
    }

    @PostMapping("/members/admin/{memberId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String rejectMember(@PathVariable Long memberId,
                               RedirectAttributes redirectAttributes) {
        try {
            memberApplicationService.rejectMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage", "회원이 거부되었습니다.");
        } catch (Exception e) {
            log.error("회원 거부 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/pending";
    }

    @PostMapping("/members/admin/{memberId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeRole(@PathVariable Long memberId,
                             @RequestParam Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            memberApplicationService.changeRole(memberId, role);
            redirectAttributes.addFlashAttribute("successMessage", "권한이 변경되었습니다.");
        } catch (Exception e) {
            log.error("권한 변경 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/list";
    }

    @GetMapping("/members/password/change")
    public String changePasswordForm(HttpServletRequest request, Model model) {
        try {
            Long memberId = extractMemberIdFromToken(request);
            if (memberId == null) {
                log.warn("토큰에서 사용자 ID를 찾을 수 없음");
                return "redirect:/members/login";
            }

            model.addAttribute("passwordChangeRequest", new MemberPasswordChangeRequest());
            model.addAttribute("todayChangeCount",
                    memberApplicationService.getTodayPasswordChangeCount(memberId));
            model.addAttribute("isPasswordChangeRequired",
                    memberApplicationService.isPasswordChangeRequired(memberId));
            model.addAttribute("isPasswordChangeRecommended",
                    memberApplicationService.isPasswordChangeRecommended(memberId));
            return "member/password-change";
        } catch (Exception e) {
            log.error("비밀번호 변경 폼 로드 중 오류: {}", e.getMessage());
            return "redirect:/members/login";
        }
    }

    @PostMapping("/members/password/change")
    public String changePassword(HttpServletRequest request,
                                 @Valid @ModelAttribute MemberPasswordChangeRequest passwordChangeRequest,
                                 BindingResult bindingResult,
                                 HttpServletRequest httpRequest,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/password-change";
        }

        try {
            Long memberId = extractMemberIdFromToken(request);
            if (memberId == null) {
                log.warn("토큰에서 사용자 ID를 찾을 수 없음");
                return "redirect:/members/login";
            }

            memberApplicationService.changePassword(memberId, passwordChangeRequest);

            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");

            log.info("비밀번호 변경 후 세션 무효화 완료 - memberId: {}", memberId);

            return "redirect:/members/login";

        } catch (BusinessException e) {
            log.warn("비밀번호 변경 실패 - error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/password/change";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}