package com.antock.api.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.value.Role;
import com.fasterxml.jackson.core.ErrorReportConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberWebController {

    private final MemberApplicationService memberApplicationService;

    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("memberJoinRequest", new MemberJoinRequest());
        return "member/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute MemberJoinRequest request,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/join";
        }

        try {
            memberApplicationService.join(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "회원가입이 완료되었습니다. 관리자 승인 후 로그인이 가능합니다.");
            return "redirect:/members/login";
        } catch (Exception e) {
            bindingResult.reject("joinFailed", e.getMessage());
            return "member/join";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @GetMapping("/profile")
    public String profile(@CurrentUser AuthenticatedUser user, Model model) {
        MemberResponse member = memberApplicationService.getMemberInfo(user.getId());
        model.addAttribute("member", member);
        ErrorReportConfiguration MemberUpdateRequest;
        model.addAttribute("memberUpdateRequest", MemberUpdateRequest.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .build());
        return "member/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@CurrentUser AuthenticatedUser user,
                                @Valid @ModelAttribute MemberUpdateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "member/profile";
        }

        try {
            memberApplicationService.updateProfile(user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "프로필이 수정되었습니다.");
            return "redirect:/members/profile";
        } catch (Exception e) {
            bindingResult.reject("updateFailed", e.getMessage());
            return "member/profile";
        }
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String memberList(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<MemberResponse> members = memberApplicationService.getMembers(pageable);
        model.addAttribute("members", members);
        return "member/admin/list";
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String pendingMembers(@PageableDefault(size = 20) Pageable pageable, Model model) {
        Page<MemberResponse> pendingMembers = memberApplicationService.getPendingMembers(pageable);
        model.addAttribute("pendingMembers", pendingMembers);
        return "member/admin/pending";
    }

    @PostMapping("/admin/{memberId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String approveMember(@PathVariable Long memberId,
                                @CurrentUser AuthenticatedUser user,
                                RedirectAttributes redirectAttributes) {
        try {
            memberApplicationService.approveMember(memberId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "회원이 승인되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/pending";
    }

    @PostMapping("/admin/{memberId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String rejectMember(@PathVariable Long memberId,
                               RedirectAttributes redirectAttributes) {
        try {
            memberApplicationService.rejectMember(memberId);
            redirectAttributes.addFlashAttribute("successMessage", "회원이 거부되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/pending";
    }

    @PostMapping("/admin/{memberId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeRole(@PathVariable Long memberId,
                             @RequestParam Role role,
                             RedirectAttributes redirectAttributes) {
        try {
            memberApplicationService.changeRole(memberId, role);
            redirectAttributes.addFlashAttribute("successMessage", "권한이 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/members/admin/list";
    }
}