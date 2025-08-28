package com.antock.web.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.dto.response.PasswordStatusResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.application.service.PasswordMigrationService;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import com.antock.global.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordMigrationService passwordMigrationService;

    @PostConstruct
    public void initializePasswords() {
        try {
            passwordMigrationService.migratePlainTextPasswords();
            log.info("비밀번호 마이그레이션 완료");
        } catch (Exception e) {
            log.error("비밀번호 마이그레이션 실패: {}", e.getMessage());
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
            log.info("회원가입 시도: username={}, email={}", joinRequest.getUsername(), joinRequest.getEmail());

            MemberResponse member = memberApplicationService.join(joinRequest);

            log.info("회원가입 성공: username={}, memberId={}", member.getUsername(), member.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "회원가입이 완료되었습니다. 관리자 승인 후 로그인이 가능합니다.");
            return "redirect:/members/login";

        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            model.addAttribute("memberJoinRequest", joinRequest);
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/";
    }

    @GetMapping("/members/login")
    public String loginForm() {
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
            accessTokenCookie.setSecure(false);
            response.addCookie(accessTokenCookie);

            log.info("웹 로그인 성공: username={}", username);

            redirectAttributes.addFlashAttribute("successMessage", "로그인 되었습니다.");
            return "redirect:/";

        } catch (Exception e) {
            log.error("웹 로그인 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/login";
        }
    }

    @GetMapping("/members/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        SecurityContextHolder.clearContext();

        redirectAttributes.addFlashAttribute("successMessage", "로그아웃 되었습니다.");
        return "redirect:/";
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

            MemberResponse member = memberApplicationService.getMemberInfo(memberId);

            if (member.getStatus() == MemberStatus.WITHDRAWN) {
                log.warn("탈퇴된 회원의 프로필 접근 시도 - memberId: {}", memberId);
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
            log.error("프로필 페이지 로드 실패: {}", e.getMessage());
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
                log.error("프로필 수정 실패 시 회원 정보 조회 오류: {}", e.getMessage());
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
            log.error("프로필 수정 실패: {}", e.getMessage());

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
                log.error("프로필 수정 실패 후 회원 정보 조회 오류: {}", ex.getMessage());
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
            log.info("회원 목록 조회 시작 - status: {}, role: {}", status, role);

            Page<MemberResponse> members;
            if ((status != null && !status.isEmpty()) || (role != null && !role.isEmpty())) {
                members = memberApplicationService.getMembersByStatusAndRole(status, role, pageable);
            } else {
                members = memberApplicationService.getMembers(pageable);
            }

            log.info("회원 목록 조회 완료 - 총 {}명, 현재 페이지 {}명",
                    members.getTotalElements(), members.getContent().size());

            model.addAttribute("memberViewList", members.getContent().stream()
                    .map(member -> new MemberView(member))
                    .collect(Collectors.toList()));

            model.addAttribute("currentPage", members.getNumber());
            model.addAttribute("totalPages", members.getTotalPages());
            model.addAttribute("totalElements", members.getTotalElements());
            model.addAttribute("size", members.getSize());
            model.addAttribute("hasNext", members.hasNext());
            model.addAttribute("hasPrevious", members.hasPrevious());
            model.addAttribute("isFirst", members.isFirst());
            model.addAttribute("isLast", members.isLast());

            long totalMembers = memberApplicationService.countMembersByStatus(null);
            long approvedMembers = memberApplicationService.countMembersByStatus(MemberStatus.APPROVED);
            long pendingMembers = memberApplicationService.countMembersByStatus(MemberStatus.PENDING);
            long rejectedMembers = memberApplicationService.countMembersByStatus(MemberStatus.REJECTED);
            long suspendedMembers = memberApplicationService.countMembersByStatus(MemberStatus.SUSPENDED);
            long withdrawnMembers = memberApplicationService.countMembersByStatus(MemberStatus.WITHDRAWN);

            model.addAttribute("totalMembers", totalMembers);
            model.addAttribute("approvedMembers", approvedMembers);
            model.addAttribute("pendingMembers", pendingMembers);
            model.addAttribute("rejectedMembers", rejectedMembers);
            model.addAttribute("suspendedMembers", suspendedMembers);
            model.addAttribute("withdrawnMembers", withdrawnMembers);

            model.addAttribute("statusList", MemberStatus.values());
            model.addAttribute("roleList", Role.values());
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedRole", role);

            return "member/admin/list";

        } catch (Exception e) {
            log.error("회원 목록 조회 실패: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "회원 목록을 불러오는 중 오류가 발생했습니다.");
            return "member/admin/list";
        }
    }

    @GetMapping("/members/admin/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String pendingMembers(@PageableDefault(size = 20) Pageable pageable, Model model) {
        try {
            log.info("승인 대기 회원 목록 조회 시작");

            Page<MemberResponse> pendingMembers = memberApplicationService.getPendingMembers(pageable);
            log.info("조회된 승인 대기 회원 수: {}", pendingMembers.getTotalElements());

            model.addAttribute("pendingMemberViewList", pendingMembers.getContent().stream()
                    .map(member -> new MemberView(member))
                    .collect(Collectors.toList()));

            model.addAttribute("currentPage", pendingMembers.getNumber());
            model.addAttribute("totalPages", pendingMembers.getTotalPages());
            model.addAttribute("totalElements", pendingMembers.getTotalElements());
            model.addAttribute("size", pendingMembers.getSize());
            model.addAttribute("hasNext", pendingMembers.hasNext());
            model.addAttribute("hasPrevious", pendingMembers.hasPrevious());
            model.addAttribute("isFirst", pendingMembers.isFirst());
            model.addAttribute("isLast", pendingMembers.isLast());

            long totalPendingCount = memberApplicationService.countMembersByStatus(MemberStatus.PENDING);
            log.info("전체 승인 대기 회원 수: {}", totalPendingCount);

            model.addAttribute("totalPendingCount", totalPendingCount);

            return "member/admin/pending";

        } catch (Exception e) {
            log.error("승인 대기 회원 목록 조회 실패: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "승인 대기 회원 목록을 불러오는 중 오류가 발생했습니다.");
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
            log.info("회원 승인 완료: memberId={}, approverId={}", memberId, approverId);

            redirectAttributes.addFlashAttribute("successMessage",
                    approvedMember.getUsername() + " 회원이 승인되었습니다.");

        } catch (Exception e) {
            log.error("회원 승인 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/members/admin/pending";
    }

    @PostMapping("/members/admin/{memberId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String rejectMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            MemberResponse rejectedMember = memberApplicationService.rejectMember(memberId);
            log.info("회원 거부 완료: memberId={}", memberId);

            redirectAttributes.addFlashAttribute("successMessage",
                    rejectedMember.getUsername() + " 회원이 거부되었습니다.");

        } catch (Exception e) {
            log.error("회원 거부 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/members/admin/pending";
    }

    @PostMapping("/members/admin/{memberId}/reset-to-pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String resetToPending(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            MemberResponse resetMember = memberApplicationService.resetToPending(memberId);
            log.info("회원 승인 대기로 변경 완료: memberId={}", memberId);

            redirectAttributes.addFlashAttribute("successMessage",
                    resetMember.getUsername() + " 회원이 승인 대기 상태로 변경되었습니다.");

        } catch (Exception e) {
            log.error("회원 승인 대기 변경 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/members/admin/list";
    }

    @PostMapping("/members/admin/{memberId}/suspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String suspendMember(@PathVariable Long memberId, RedirectAttributes redirectAttributes) {
        try {
            MemberResponse suspendedMember = memberApplicationService.suspendMember(memberId);
            log.info("회원 정지 완료: memberId={}", memberId);

            redirectAttributes.addFlashAttribute("successMessage",
                    suspendedMember.getUsername() + " 회원이 정지되었습니다.");

        } catch (Exception e) {
            log.error("회원 정지 실패: {}", e.getMessage());
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
            log.error("비밀번호 변경 페이지 로드 실패: {}", e.getMessage());
            return "redirect:/members/profile";
        }
    }

    @PostMapping("/members/password/change")
    public String changePassword(HttpServletRequest request,
            @Valid @ModelAttribute("passwordChangeRequest") MemberPasswordChangeRequest passwordChangeRequest,
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
                        PasswordStatusResponse passwordStatus = memberApplicationService.getPasswordStatus(memberId);
                        model.addAttribute("isPasswordChangeRequired", passwordStatus.isChangeRequired());
                        model.addAttribute("isPasswordChangeRecommended", passwordStatus.isChangeRecommended());
                        model.addAttribute("todayChangeCount", passwordStatus.getTodayChangeCount());
                    }
                }
            } catch (Exception e) {
                log.error("비밀번호 변경 실패 시 상태 조회 오류: {}", e.getMessage());
            }

            model.addAttribute("passwordChangeRequest", passwordChangeRequest);
            model.addAttribute("org.springframework.validation.BindingResult.passwordChangeRequest", bindingResult);
            return "member/password-change";
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

            memberApplicationService.changePassword(memberId, passwordChangeRequest);
            log.info("비밀번호 변경 성공: memberId={}", memberId);

            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/members/profile";

        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());

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
                log.error("비밀번호 변경 실패 후 상태 조회 오류: {}", ex.getMessage());
            }

            model.addAttribute("passwordChangeRequest", passwordChangeRequest);
            model.addAttribute("errorMessage", e.getMessage());
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
}