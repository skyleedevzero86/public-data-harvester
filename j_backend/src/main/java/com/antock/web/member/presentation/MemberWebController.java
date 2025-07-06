package com.antock.web.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.AuthTokenService;
import com.antock.api.member.value.MemberStatus;
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
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberWebController {

    private final MemberApplicationService memberApplicationService;
    private final AuthTokenService authTokenService;

    private Date convertToDate(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

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

            if (member.getStatus() == MemberStatus.WITHDRAWN) {
                log.warn("탈퇴된 회원의 프로필 접근 시도 - memberId: {}", memberId);
                request.getSession().invalidate();
                SecurityContextHolder.clearContext();
                return "redirect:/";
            }

            model.addAttribute("member", member);

            model.addAttribute("createDateFormatted", convertToDate(member.getCreateDate()));
            model.addAttribute("lastLoginAtFormatted", convertToDate(member.getLastLoginAt()));
            model.addAttribute("approvedAtFormatted", convertToDate(member.getApprovedAt()));
            model.addAttribute("passwordChangedAtFormatted", convertToDate(member.getPasswordChangedAt()));

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
                    model.addAttribute("createDateFormatted", convertToDate(member.getCreateDate()));
                    model.addAttribute("lastLoginAtFormatted", convertToDate(member.getLastLoginAt()));
                    model.addAttribute("approvedAtFormatted", convertToDate(member.getApprovedAt()));
                    model.addAttribute("passwordChangedAtFormatted", convertToDate(member.getPasswordChangedAt()));
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
                    model.addAttribute("createDateFormatted", convertToDate(member.getCreateDate()));
                    model.addAttribute("lastLoginAtFormatted", convertToDate(member.getLastLoginAt()));
                    model.addAttribute("approvedAtFormatted", convertToDate(member.getApprovedAt()));
                    model.addAttribute("passwordChangedAtFormatted", convertToDate(member.getPasswordChangedAt()));
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
    public String memberList(@PageableDefault(size = 20) Pageable pageable,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "role", required = false) String role,
                             Model model) {

        log.info("회원 목록 조회 - status: {}, role: {}", status, role);

        Page<MemberResponse> members;
        if (status != null || role != null) {
            members = memberApplicationService.getMembersByStatusAndRole(status, role, pageable);
            log.info("필터링 조회 결과: {} 건", members.getTotalElements());
        } else {
            members = memberApplicationService.getMembers(pageable);
            log.info("전체 조회 결과: {} 건", members.getTotalElements());
        }

        List<Map<String, Object>> memberViewList = members.getContent().stream()
                .map(member -> {
                    Map<String, Object> memberView = new HashMap<>();
                    memberView.put("member", member);
                    memberView.put("createDateFormatted", convertToDate(member.getCreateDate()));
                    memberView.put("lastLoginAtFormatted", convertToDate(member.getLastLoginAt()));
                    memberView.put("approvedAtFormatted", convertToDate(member.getApprovedAt()));
                    return memberView;
                })
                .collect(Collectors.toList());

        long rejectedCount = memberApplicationService.countMembersByStatus(MemberStatus.REJECTED);
        long withdrawnCount = memberApplicationService.countMembersByStatus(MemberStatus.WITHDRAWN);

        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("withdrawnCount", withdrawnCount);
        model.addAttribute("memberViewList", memberViewList);
        model.addAttribute("members", members);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedRole", role);

        return "member/admin/list";
    }

    @GetMapping("/members/admin/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String pendingMembers(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<MemberResponse> pendingMembers = memberApplicationService.getPendingMembers(pageable);

        List<Map<String, Object>> pendingMemberViewList = pendingMembers.getContent().stream()
                .map(member -> {
                    Map<String, Object> memberView = new HashMap<>();
                    memberView.put("member", member);
                    memberView.put("createDateFormatted", convertToDate(member.getCreateDate()));
                    memberView.put("lastLoginAtFormatted", convertToDate(member.getLastLoginAt()));
                    memberView.put("approvedAtFormatted", convertToDate(member.getApprovedAt()));
                    return memberView;
                })
                .collect(Collectors.toList());

        model.addAttribute("pendingMemberViewList", pendingMemberViewList);
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

    @PostMapping("/members/admin/{memberId}/unlock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String unlockMember(@PathVariable Long memberId,
                               RedirectAttributes redirectAttributes) {
        try {
            log.info("관리자 계정 정지 해제 요청 - memberId: {}", memberId);

            MemberResponse member = memberApplicationService.unlockMember(memberId);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("회원 '%s'의 계정 정지가 해제되었습니다.", member.getUsername()));

            log.info("관리자 계정 정지 해제 완료 - memberId: {}, username: {}",
                    memberId, member.getUsername());

        } catch (Exception e) {
            log.error("계정 정지 해제 실패 - memberId: {}, error: {}", memberId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "계정 정지 해제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/members/admin/list";
    }

    @PostMapping("/members/admin/{memberId}/suspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String suspendMemberManual(@PathVariable Long memberId,
                                      RedirectAttributes redirectAttributes) {
        try {
            log.info("관리자 수동 회원 정지 요청 - memberId: {}", memberId);

            MemberResponse member = memberApplicationService.suspendMember(memberId);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("회원 '%s'이(가) 정지되었습니다.", member.getUsername()));

            log.warn("관리자 수동 회원 정지 완료 - memberId: {}, username: {}",
                    memberId, member.getUsername());

        } catch (Exception e) {
            log.error("회원 정지 실패 - memberId: {}, error: {}", memberId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "회원 정지 중 오류가 발생했습니다: " + e.getMessage());
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
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        Long memberId = null;
        try {
            memberId = extractMemberIdFromToken(request);
            if (memberId == null) {
                log.warn("토큰에서 사용자 ID를 찾을 수 없음");
                return "redirect:/members/login";
            }
        } catch (Exception e) {
            log.error("토큰 파싱 실패: {}", e.getMessage());
            return "redirect:/members/login";
        }

        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("passwordChangeRequest", passwordChangeRequest);
                model.addAttribute("todayChangeCount",
                        memberApplicationService.getTodayPasswordChangeCount(memberId));
                model.addAttribute("isPasswordChangeRequired",
                        memberApplicationService.isPasswordChangeRequired(memberId));
                model.addAttribute("isPasswordChangeRecommended",
                        memberApplicationService.isPasswordChangeRecommended(memberId));
                return "member/password-change";
            } catch (Exception e) {
                log.error("비밀번호 변경 폼 에러 처리 중 오류: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "오류가 발생했습니다. 다시 시도해주세요.");
                return "redirect:/members/profile";
            }
        }

        try {
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

            try {
                model.addAttribute("passwordChangeRequest", passwordChangeRequest);
                model.addAttribute("todayChangeCount",
                        memberApplicationService.getTodayPasswordChangeCount(memberId));
                model.addAttribute("isPasswordChangeRequired",
                        memberApplicationService.isPasswordChangeRequired(memberId));
                model.addAttribute("isPasswordChangeRecommended",
                        memberApplicationService.isPasswordChangeRecommended(memberId));
                model.addAttribute("errorMessage", e.getMessage());
                return "member/password-change";
            } catch (Exception ex) {
                log.error("비밀번호 변경 에러 처리 중 오류: {}", ex.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/members/profile";
            }
        } catch (Exception e) {
            log.error("비밀번호 변경 중 예상치 못한 오류: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 변경 중 오류가 발생했습니다.");
            return "redirect:/members/profile";
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

    @PostMapping("/members/admin/{memberId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            memberApplicationService.deleteMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage", "회원이 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/list";
    }

    @PostMapping("/members/withdraw")
    public String withdraw(HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes) {
        try {
            Long memberId = extractMemberIdFromToken(request);
            if (memberId == null) {
                log.warn("탈퇴 시도 - 토큰에서 사용자 ID를 찾을 수 없음");
                redirectAttributes.addFlashAttribute("errorMessage", "인증 정보를 찾을 수 없습니다.");
                return "redirect:/members/login";
            }

            log.info("회원 탈퇴 요청 - memberId: {}", memberId);

            memberApplicationService.deleteMember(memberId);

            log.info("회원 탈퇴 처리 완료 - memberId: {}", memberId);

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

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            SecurityContextHolder.clearContext();

            log.info("회원 탈퇴 완료 및 로그아웃 처리 - memberId: {}", memberId);

            redirectAttributes.addFlashAttribute("successMessage", "회원 탈퇴가 완료되었습니다.");
            return "redirect:/";

        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 오류: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/members/profile";
        }
    }
}