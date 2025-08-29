package com.antock.web.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.dto.response.PasswordStatusResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.PasswordMigrationService;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
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

    @PostConstruct
    public void initPasswordMigration() {
        try {
            passwordMigrationService.migratePlainTextPasswords();
        } catch (Exception e) {
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

            long totalMembers = memberApplicationService.countAllMembers();
            long pendingMembers = memberApplicationService.countMembersByStatus(MemberStatus.PENDING);
            long approvedMembers = memberApplicationService.countMembersByStatus(MemberStatus.APPROVED);
            long rejectedMembers = memberApplicationService.countMembersByStatus(MemberStatus.REJECTED);
            long suspendedMembers = memberApplicationService.countMembersByStatus(MemberStatus.SUSPENDED);
            long withdrawnMembers = memberApplicationService.countMembersByStatus(MemberStatus.WITHDRAWN);

            model.addAttribute("totalMembers", totalMembers);
            model.addAttribute("pendingMembers", pendingMembers);
            model.addAttribute("approvedMembers", approvedMembers);
            model.addAttribute("rejectedMembers", rejectedMembers);
            model.addAttribute("suspendedMembers", suspendedMembers);
            model.addAttribute("withdrawnMembers", withdrawnMembers);

            return "member/admin/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/admin/list";
        }
    }

    @GetMapping("/members/admin/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String pendingMembers(@PageableDefault(size = 20) Pageable pageable, Model model) {
        try {
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
            }

            model.addAttribute("passwordChangeRequest", passwordChangeRequest);
            model.addAttribute("errorMessage", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
            return "member/password-change";
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

}