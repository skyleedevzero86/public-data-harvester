package com.antock.web.member.presentation;

import com.antock.api.member.application.dto.request.PasswordFindRequest;
import com.antock.api.member.application.dto.request.PasswordResetRequest;
import com.antock.api.member.application.service.PasswordFindService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/members/password")
@RequiredArgsConstructor
public class MemberPasswordFindWebController {

    private final PasswordFindService passwordFindService;

    @GetMapping("/find")
    public String findPasswordForm(Model model) {
        model.addAttribute("passwordFindRequest", new PasswordFindRequest());
        return "member/password-find";
    }

    @PostMapping("/find")
    public String processFindPassword(@Valid @ModelAttribute PasswordFindRequest request,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return "member/password-find";
        }

        try {
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            passwordFindService.requestPasswordReset(request, clientIp, userAgent);

            redirectAttributes.addFlashAttribute("successMessage",
                    "비밀번호 재설정 링크가 이메일로 전송되었습니다.");
            return "redirect:/members/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/password/find";
        }
    }

    @GetMapping("/reset")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        try {
            if (!passwordFindService.validateResetToken(token)) {
                model.addAttribute("errorMessage", "유효하지 않거나 만료된 링크입니다.");
                return "member/password-reset-error";
            }

            model.addAttribute("token", token);
            model.addAttribute("passwordResetRequest", new PasswordResetRequest());
            return "member/password-reset";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/password-reset-error";
        }
    }

    @PostMapping("/reset")
    public String processResetPassword(@Valid @ModelAttribute PasswordResetRequest request,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return "member/password-reset";
        }

        try {
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            passwordFindService.resetPassword(request, clientIp, userAgent);

            redirectAttributes.addFlashAttribute("successMessage",
                    "비밀번호가 성공적으로 재설정되었습니다.");
            return "redirect:/members/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "member/password-reset";
        }
    }

    @GetMapping("/find/success")
    public String findPasswordSuccess(Model model) {
        return "member/password-find-success";
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