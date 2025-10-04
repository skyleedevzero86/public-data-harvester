package com.antock.web.member.presentation;

import com.antock.api.member.application.dto.request.*;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.dto.response.PasswordFindResponse;
import com.antock.api.member.application.dto.response.PasswordStatusResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.PasswordFindService;
import com.antock.api.member.application.service.PasswordMigrationService;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
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

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberWebController {

    private final MemberApplicationService memberApplicationService;
    private final PasswordMigrationService passwordMigrationService;
    private final PasswordFindService passwordFindService;

    @PostConstruct
    public void initPasswordMigration() {
        try {
            passwordMigrationService.migratePlainTextPasswords();
        } catch (Exception e) {
            log.error("Password migration failed", e);
        }
    }

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
    public String processJoin(@Valid @ModelAttribute MemberJoinRequest joinRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("memberJoinRequest", joinRequest);
            return "member/join";
        }

        try {
            MemberResponse member = memberApplicationService.join(joinRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "회원가입이 완료되었습니다. 관리자 승인 후 로그인이 가능합니다.");
            return "redirect:/members/login";
        } catch (Exception e) {
            model.addAttribute("memberJoinRequest", joinRequest);
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";
        }
    }

    @GetMapping("/members/login")
    public String loginForm(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        return "member/login";
    }

    @PostMapping("/members/login")
    public String processLogin(@RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        try {
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
            accessTokenCookie.setSecure(false);
            response.addCookie(accessTokenCookie);

            redirectAttributes.addFlashAttribute("successMessage", "로그인 되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/login";
        }
    }

    @GetMapping("/members/logout")
    public String logout(HttpServletResponse response, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        clearAllAuthCookies(response);
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("successMessage", "로그아웃 되었습니다.");
        return "redirect:/members/login";
    }

    private void clearAllAuthCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        response.addCookie(refreshTokenCookie);
    }

    @GetMapping("/members/profile")
    public String profile(Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/members/login";
            }

            String username = authentication.getName();
            Long memberId = memberApplicationService.getMemberIdByUsername(username);
            if (memberId == null) {
                return "redirect:/members/login";
            }

            MemberResponse member = memberApplicationService.getCurrentMemberInfo(memberId);
            if (member == null) {
                return "redirect:/members/login";
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
            return "redirect:/";
        }
    }

    @PostMapping("/members/profile")
    public String updateProfile(@Valid @ModelAttribute MemberUpdateRequest updateRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    String username = authentication.getName();
                    Long memberId = memberApplicationService.getMemberIdByUsername(username);
                    if (memberId != null) {
                        MemberResponse member = memberApplicationService.getMemberInfo(memberId);
                        model.addAttribute("member", member);
                        model.addAttribute("createDateFormatted", convertToDate(member.getCreateDate()));
                        model.addAttribute("lastLoginAtFormatted", convertToDate(member.getLastLoginAt()));
                        model.addAttribute("approvedAtFormatted", convertToDate(member.getApprovedAt()));
                        model.addAttribute("passwordChangedAtFormatted", convertToDate(member.getPasswordChangedAt()));
                    }
                }
            } catch (Exception e) {
                log.error("Error loading member info for profile update", e);
            }

            model.addAttribute("memberUpdateRequest", updateRequest);
            model.addAttribute("org.springframework.validation.BindingResult.memberUpdateRequest", bindingResult);
            return "member/profile";
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "회원 정보를 찾을 수 없습니다.");
            }

            String username = authentication.getName();
            Long memberId = memberApplicationService.getMemberIdByUsername(username);
            if (memberId == null) {
                throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "회원 정보를 찾을 수 없습니다.");
            }

            memberApplicationService.updateProfile(memberId, updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "프로필이 성공적으로 수정되었습니다.");
            return "redirect:/members/profile";
        } catch (Exception e) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    String username = authentication.getName();
                    Long memberId = memberApplicationService.getMemberIdByUsername(username);
                    if (memberId != null) {
                        MemberResponse member = memberApplicationService.getMemberInfo(memberId);
                        model.addAttribute("member", member);
                        model.addAttribute("createDateFormatted", convertToDate(member.getCreateDate()));
                        model.addAttribute("lastLoginAtFormatted", convertToDate(member.getLastLoginAt()));
                        model.addAttribute("approvedAtFormatted", convertToDate(member.getApprovedAt()));
                        model.addAttribute("passwordChangedAtFormatted", convertToDate(member.getPasswordChangedAt()));
                    }
                }
            } catch (Exception ex) {
                log.error("Error loading member info for profile update", ex);
            }

            model.addAttribute("memberUpdateRequest", updateRequest);
            model.addAttribute("errorMessage", e.getMessage());
            return "member/profile";
        }
    }

    @GetMapping("/members/admin/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String memberList(@PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            Model model) {
        try {
            Page<MemberResponse> members = memberApplicationService.getMembersByStatusAndRole(status, role, pageable);

            List<MemberView> memberViewList = members.getContent().stream()
                    .map(member -> new MemberView(member))
                    .collect(Collectors.toList());

            long pendingCount = memberApplicationService.countMembersByStatus(MemberStatus.PENDING);
            long approvedCount = memberApplicationService.countMembersByStatus(MemberStatus.APPROVED);
            long rejectedCount = memberApplicationService.countMembersByStatus(MemberStatus.REJECTED);
            long suspendedCount = memberApplicationService.countMembersByStatus(MemberStatus.SUSPENDED);
            long withdrawnCount = memberApplicationService.countMembersByStatus(MemberStatus.WITHDRAWN);

            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("approvedCount", approvedCount);
            model.addAttribute("rejectedCount", rejectedCount);
            model.addAttribute("suspendedCount", suspendedCount);
            model.addAttribute("withdrawnCount", withdrawnCount);

            model.addAttribute("memberViewList", memberViewList);
            model.addAttribute("members", members);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedRole", role);

            return "member/admin/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/admin/list";
        }
    }

    @GetMapping("/members/admin/pending")
    public String pendingMembers(@PageableDefault(size = 20) Pageable pageable, 
                                Model model, 
                                @CurrentUser AuthenticatedUser user) {
        try {
            if (user == null || (!user.getRole().equals("ADMIN") && !user.getRole().equals("MANAGER"))) {
                model.addAttribute("error", "접근 권한이 없습니다. 관리자 또는 매니저 권한이 필요합니다.");
                model.addAttribute("errorCode", "ACCESS_DENIED");
                return "member/error";
            }

            Page<MemberResponse> pendingMembers = memberApplicationService.getPendingMembers(pageable);

            List<MemberView> pendingMemberViewList = pendingMembers.getContent().stream()
                    .map(member -> new MemberView(member))
                    .collect(Collectors.toList());

            model.addAttribute("pendingMemberViewList", pendingMemberViewList);
            model.addAttribute("pendingMembers", pendingMembers);

            long totalPendingCount = memberApplicationService.countMembersByStatus(MemberStatus.PENDING);
            model.addAttribute("totalPendingCount", totalPendingCount);

            return "member/admin/pending";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/admin/pending";
        }
    }

    @PostMapping("/members/admin/{memberId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String approveMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Long approverId = memberApplicationService.getMemberIdByUsername(username);

            MemberResponse approvedMember = memberApplicationService.approveMember(memberId, approverId);
            redirectAttributes.addFlashAttribute("successMessage",
                    approvedMember.getUsername() + " 회원이 승인되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/list";
    }

    @PostMapping("/members/admin/{memberId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String rejectMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            MemberResponse rejectedMember = memberApplicationService.rejectMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage",
                    rejectedMember.getUsername() + " 회원이 거부되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/list";
    }

    @PostMapping("/members/admin/{memberId}/reset-to-pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String resetToPending(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            MemberResponse resetMember = memberApplicationService.resetToPending(memberId);
            redirectAttributes.addFlashAttribute("successMessage",
                    resetMember.getUsername() + " 회원이 승인 대기 상태로 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/list";
    }

    @PostMapping("/members/admin/{memberId}/suspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String suspendMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            MemberResponse suspendedMember = memberApplicationService.suspendMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage",
                    suspendedMember.getUsername() + " 회원이 정지되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/list";
    }

    @GetMapping("/members/password/change")
    public String changePasswordForm(HttpServletRequest request, Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/members/login";
            }

            String username = authentication.getName();
            Long memberId = memberApplicationService.getMemberIdByUsername(username);
            if (memberId == null) {
                return "redirect:/members/login";
            }

            PasswordStatusResponse passwordStatus = memberApplicationService.getPasswordStatus(memberId);

            model.addAttribute("passwordChangeRequest", new MemberPasswordChangeRequest());
            model.addAttribute("isPasswordChangeRequired", passwordStatus.isChangeRequired());
            model.addAttribute("isPasswordChangeRecommended", passwordStatus.isChangeRecommended());
            model.addAttribute("todayChangeCount", passwordStatus.getTodayChangeCount());

            return "member/password-change";
        } catch (Exception e) {
            return "redirect:/members/profile";
        }
    }

    @PostMapping("/members/password/change")
    public String changePassword(HttpServletRequest request,
            @Valid @ModelAttribute("passwordChangeRequest") MemberPasswordChangeRequest passwordChangeRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/members/login";
            }

            String username = authentication.getName();
            Long memberId = memberApplicationService.getMemberIdByUsername(username);
            if (memberId == null) {
                return "redirect:/members/login";
            }

            PasswordStatusResponse passwordStatus = memberApplicationService.getPasswordStatus(memberId);

            if (bindingResult.hasErrors()) {
                model.addAttribute("passwordChangeRequest", passwordChangeRequest);
                model.addAttribute("isPasswordChangeRequired", passwordStatus.isChangeRequired());
                model.addAttribute("isPasswordChangeRecommended", passwordStatus.isChangeRecommended());
                model.addAttribute("todayChangeCount", passwordStatus.getTodayChangeCount());
                return "member/password-change";
            }

            if (passwordStatus.getTodayChangeCount() >= 3) {
                model.addAttribute("passwordChangeRequest", passwordChangeRequest);
                model.addAttribute("isPasswordChangeRequired", passwordStatus.isChangeRequired());
                model.addAttribute("isPasswordChangeRecommended", passwordStatus.isChangeRecommended());
                model.addAttribute("todayChangeCount", passwordStatus.getTodayChangeCount());
                model.addAttribute("errorMessage", "오늘은 더 이상 비밀번호를 변경할 수 없습니다. (일일 3회 제한)");
                return "member/password-change";
            }

            memberApplicationService.changePassword(memberId, passwordChangeRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/members/profile";
        } catch (BusinessException e) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    String username = authentication.getName();
                    Long memberId = memberApplicationService.getMemberIdByUsername(username);
                    if (memberId != null) {
                        PasswordStatusResponse passwordStatus = memberApplicationService.getPasswordStatus(memberId);
                        model.addAttribute("isPasswordChangeRequired", passwordStatus.isChangeRequired());
                        model.addAttribute("isPasswordChangeRecommended", passwordStatus.isChangeRecommended());
                        model.addAttribute("todayChangeCount", passwordStatus.getTodayChangeCount());
                    }
                }
            } catch (Exception ex) {
                log.error("Error loading password status", ex);
            }

            model.addAttribute("passwordChangeRequest", passwordChangeRequest);
            model.addAttribute("errorMessage", e.getMessage());
            return "member/password-change";
        } catch (Exception e) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    String username = authentication.getName();
                    Long memberId = memberApplicationService.getMemberIdByUsername(username);
                    if (memberId != null) {
                        PasswordStatusResponse passwordStatus = memberApplicationService.getPasswordStatus(memberId);
                        model.addAttribute("isPasswordChangeRequired", passwordStatus.isChangeRequired());
                        model.addAttribute("isPasswordChangeRecommended", passwordStatus.isChangeRecommended());
                        model.addAttribute("todayChangeCount", passwordStatus.getTodayChangeCount());
                    }
                }
            } catch (Exception ex) {
                log.error("Error loading password status", ex);
            }

            model.addAttribute("passwordChangeRequest", passwordChangeRequest);
            model.addAttribute("errorMessage", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
            return "member/password-change";
        }
    }

    @PostMapping("/members/admin/{memberId}/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String deleteMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        log.info("웹 컨트롤러: 회원 삭제(탈퇴) 요청 - memberId: {}", memberId);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            Long currentUserId = memberApplicationService.getMemberIdByUsername(currentUsername);

            MemberResponse targetMember = memberApplicationService.getMemberInfo(memberId);

            if (currentUserId.equals(memberId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "자기 자신의 계정은 삭제할 수 없습니다.");
                return "redirect:/members/admin/list";
            }

            if (targetMember.getRole() == com.antock.api.member.value.Role.ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "관리자 계정은 삭제할 수 없습니다.");
                return "redirect:/members/admin/list";
            }

            if (targetMember.getStatus() == MemberStatus.WITHDRAWN) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 탈퇴한 회원입니다.");
                return "redirect:/members/admin/list";
            }

            memberApplicationService.deleteMember(memberId);

            log.info("웹 컨트롤러: 회원 삭제(탈퇴) 완료 - memberId: {}, username: {}", memberId, targetMember.getUsername());

            redirectAttributes.addFlashAttribute("successMessage",
                    targetMember.getUsername() + " 회원이 탈퇴 처리되었습니다.");

        } catch (BusinessException e) {
            log.error("웹 컨트롤러: 회원 삭제 실패 (비즈니스 예외) - memberId: {}, error: {}", memberId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("웹 컨트롤러: 회원 삭제 실패 - memberId: {}", memberId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "회원 탈퇴 처리에 실패했습니다: " + e.getMessage());
        }

        return "redirect:/members/admin/list";
    }

    @PostMapping("/members/withdraw")
    public String withdrawSelf(HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/members/login";
            }

            String username = authentication.getName();
            Long memberId = memberApplicationService.getMemberIdByUsername(username);
            if (memberId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
                return "redirect:/members/profile";
            }

            MemberResponse currentMember = memberApplicationService.getMemberInfo(memberId);

            if (currentMember.getRole() == com.antock.api.member.value.Role.ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "관리자는 본인 탈퇴를 할 수 없습니다. 다른 관리자에게 문의하세요.");
                return "redirect:/members/profile";
            }

            if (currentMember.getStatus() == MemberStatus.WITHDRAWN) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 탈퇴한 회원입니다.");
                return "redirect:/members/profile";
            }

            log.info("사용자 본인 탈퇴 요청 - memberId: {}, username: {}", memberId, username);

            memberApplicationService.deleteMember(memberId);

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            clearAllAuthCookies(response);
            SecurityContextHolder.clearContext();

            log.info("사용자 본인 탈퇴 완료 - memberId: {}, username: {}", memberId, username);

            redirectAttributes.addFlashAttribute("successMessage",
                    "회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.");

            return "redirect:/members/login";

        } catch (BusinessException e) {
            log.error("사용자 본인 탈퇴 실패 (비즈니스 예외): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/profile";
        } catch (Exception e) {
            log.error("사용자 본인 탈퇴 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "탈퇴 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/members/profile";
        }
    }

    @PostMapping("/members/admin/{memberId}/role")
    public String changeMemberRole(@PathVariable Long memberId,
            @RequestParam Role role,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/members/login";
            }

            String username = authentication.getName();
            Long approverId = memberApplicationService.getMemberIdByUsername(username);

            if (approverId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
                return "redirect:/members/admin/list";
            }

            memberApplicationService.changeMemberRole(memberId, role, approverId);
            redirectAttributes.addFlashAttribute("successMessage", "회원 역할이 변경되었습니다.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "역할 변경에 실패했습니다: " + e.getMessage());
        }

        return "redirect:/members/admin/list";
    }

    @GetMapping("/password/reset")
    public String showPasswordResetPage(@RequestParam String token, Model model) {
        try {
            boolean isValid = passwordFindService.validateResetToken(token);
            if (!isValid) {
                model.addAttribute("error", "유효하지 않거나 만료된 링크입니다.");
                return "member/password-reset-error";
            }

            PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
            passwordResetRequest.setToken(token);
            model.addAttribute("passwordResetRequest", passwordResetRequest);
            model.addAttribute("token", token);

            return "member/password-reset";
        } catch (Exception e) {
            log.error("비밀번호 재설정 페이지 오류", e);
            model.addAttribute("error", "비밀번호 재설정 중 오류가 발생했습니다.");
            return "member/password-reset-error";
        }
    }

    @PostMapping("/password/reset")
    public String processPasswordReset(@Valid @ModelAttribute PasswordResetRequest request,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("비밀번호 재설정 요청 처리 시작 - token: {}", request.getToken());

            if (bindingResult.hasErrors()) {
                log.error("폼 검증 실패 - errors: {}", bindingResult.getAllErrors());
                model.addAttribute("token", request.getToken());
                return "member/password-reset";
            }

            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            log.info("PasswordFindService.resetPassword 호출 시작");
            passwordFindService.resetPassword(request, clientIp, userAgent);
            log.info("PasswordFindService.resetPassword 호출 완료");

            redirectAttributes.addFlashAttribute("success", "비밀번호가 성공적으로 재설정되었습니다.");
            log.info("비밀번호 재설정 성공 - 로그인 페이지로 리다이렉트");
            return "redirect:/members/login";

        } catch (BusinessException e) {
            log.error("비밀번호 재설정 실패 (비즈니스 예외) - token: {}, error: {}", request.getToken(), e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", request.getToken());
            return "member/password-reset";
        } catch (Exception e) {
            log.error("비밀번호 재설정 중 오류 발생 - token: {}", request.getToken(), e);
            model.addAttribute("error", "비밀번호 재설정 중 오류가 발생했습니다: " + e.getMessage());
            return "member/password-reset-error";
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

    public static class MemberView {
        private final MemberResponse member;

        public MemberView(MemberResponse member) {
            this.member = member;
        }

        public MemberResponse getMember() {
            return member;
        }

        public Date getCreateDateFormatted() {
            if (member.getCreateDate() == null)
                return null;
            return Date.from(member.getCreateDate().atZone(ZoneId.systemDefault()).toInstant());
        }

        public Date getLastLoginAtFormatted() {
            if (member.getLastLoginAt() == null)
                return null;
            return Date.from(member.getLastLoginAt().atZone(ZoneId.systemDefault()).toInstant());
        }

        public Date getApprovedAtFormatted() {
            if (member.getApprovedAt() == null)
                return null;
            return Date.from(member.getApprovedAt().atZone(ZoneId.systemDefault()).toInstant());
        }

        public Date getPasswordChangedAtFormatted() {
            if (member.getPasswordChangedAt() == null)
                return null;
            return Date.from(member.getPasswordChangedAt().atZone(ZoneId.systemDefault()).toInstant());
        }
    }
}