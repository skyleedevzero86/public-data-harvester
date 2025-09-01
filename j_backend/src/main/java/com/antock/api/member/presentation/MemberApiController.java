package com.antock.api.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.dto.response.PasswordStatusResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "회원 관리 및 인증 API")
public class MemberApiController {

    private final MemberApplicationService memberApplicationService;

    @PostMapping("/join")
    public ApiResponse<MemberResponse> join(@Valid @RequestBody MemberJoinRequest request) {
        log.info("회원 가입 요청: {}", request.getUsername());

        try {
            MemberResponse response = memberApplicationService.join(request);
            return ApiResponse.success(response, "회원 가입이 완료되었습니다.");
        } catch (Exception e) {
            log.error("회원 가입 실패: {}", request.getUsername(), e);
            return ApiResponse.error("회원 가입에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<MemberLoginResponse> login(
            @Valid @RequestBody MemberLoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("로그인 요청: {}", request.getUsername());

        try {
            String clientIp = getClientIp(httpRequest);
            MemberLoginResponse response = memberApplicationService.login(request, clientIp);
            return ApiResponse.success(response, "로그인이 완료되었습니다.");
        } catch (Exception e) {
            log.error("로그인 실패: {}", request.getUsername(), e);
            return ApiResponse.error("로그인에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ApiResponse<MemberResponse> getCurrentMemberInfo(@CurrentUser AuthenticatedUser user) {
        log.info("현재 사용자 정보 조회 요청: {}", user.getUsername());

        try {
            MemberResponse response = memberApplicationService.getCurrentMemberInfo(user.getId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("현재 사용자 정보 조회 실패: {}", user.getUsername(), e);
            return ApiResponse.error("사용자 정보를 가져올 수 없습니다.");
        }
    }

    @PutMapping("/profile")
    public ApiResponse<MemberResponse> updateProfile(
            @Valid @RequestBody MemberUpdateRequest request,
            @CurrentUser AuthenticatedUser user) {

        log.info("프로필 수정 요청: {}", user.getUsername());

        try {
            MemberResponse response = memberApplicationService.updateProfile(user.getId(), request);
            return ApiResponse.success(response, "프로필이 수정되었습니다.");
        } catch (Exception e) {
            log.error("프로필 수정 실패: {}", user.getUsername(), e);
            return ApiResponse.error("프로필 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody MemberPasswordChangeRequest request,
            @CurrentUser AuthenticatedUser user) {

        log.info("비밀번호 변경 요청: {}", user.getUsername());

        try {
            memberApplicationService.changePassword(user.getId(), request);
            return ApiResponse.successVoid("비밀번호가 변경되었습니다.");
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", user.getUsername(), e);
            return ApiResponse.errorVoid("비밀번호 변경에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/password/status")
    public ApiResponse<PasswordStatusResponse> getPasswordStatus(@CurrentUser AuthenticatedUser user) {
        log.info("비밀번호 상태 조회 요청: {}", user.getUsername());

        try {
            PasswordStatusResponse response = memberApplicationService.getPasswordStatus(user.getId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("비밀번호 상태 조회 실패: {}", user.getUsername(), e);
            return ApiResponse.error("비밀번호 상태를 확인할 수 없습니다.");
        }
    }

    @GetMapping("/admin")
    public ApiResponse<Page<MemberResponse>> getMembers(Pageable pageable) {
        log.info("회원 목록 조회 요청 (관리자)");

        try {
            Page<MemberResponse> members = memberApplicationService.getMembers(pageable);
            return ApiResponse.success(members);
        } catch (Exception e) {
            log.error("회원 목록 조회 실패 (관리자)", e);
            return ApiResponse.error("회원 목록을 가져올 수 없습니다.");
        }
    }

    @GetMapping("/admin/pending")
    public ApiResponse<Page<MemberResponse>> getPendingMembers(Pageable pageable) {
        log.info("승인 대기 회원 목록 조회 요청");

        try {
            Page<MemberResponse> members = memberApplicationService.getPendingMembers(pageable);
            return ApiResponse.success(members);
        } catch (Exception e) {
            log.error("승인 대기 회원 목록 조회 실패", e);
            return ApiResponse.error("승인 대기 회원 목록을 가져올 수 없습니다.");
        }
    }

    @PostMapping("/admin/{memberId}/approve")
    public ApiResponse<MemberResponse> approveMember(
            @PathVariable Long memberId,
            @CurrentUser AuthenticatedUser approver) {

        log.info("회원 승인 요청 - memberId: {}, approver: {}", memberId, approver.getUsername());

        try {
            MemberResponse response = memberApplicationService.approveMember(memberId, approver.getId());
            return ApiResponse.success(response, "회원이 승인되었습니다.");
        } catch (Exception e) {
            log.error("회원 승인 실패 - memberId: {}", memberId, e);
            return ApiResponse.error("회원 승인에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/reject")
    public ApiResponse<MemberResponse> rejectMember(@PathVariable Long memberId) {
        log.info("회원 거부 요청 - memberId: {}", memberId);

        try {
            MemberResponse response = memberApplicationService.rejectMember(memberId);
            return ApiResponse.success(response, "회원이 거부되었습니다.");
        } catch (Exception e) {
            log.error("회원 거부 실패 - memberId: {}", memberId, e);
            return ApiResponse.error("회원 거부에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/suspend")
    public ApiResponse<MemberResponse> suspendMember(@PathVariable Long memberId) {
        log.info("회원 정지 요청 - memberId: {}", memberId);

        try {
            MemberResponse response = memberApplicationService.suspendMember(memberId);
            return ApiResponse.success(response, "회원이 정지되었습니다.");
        } catch (Exception e) {
            log.error("회원 정지 실패 - memberId: {}", memberId, e);
            return ApiResponse.error("회원 정지에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/unlock")
    public ApiResponse<MemberResponse> unlockMember(@PathVariable Long memberId) {
        log.info("회원 정지 해제 요청 - memberId: {}", memberId);

        try {
            MemberResponse response = memberApplicationService.unlockMember(memberId);
            return ApiResponse.success(response, "회원 정지가 해제되었습니다.");
        } catch (Exception e) {
            log.error("회원 정지 해제 실패 - memberId: {}", memberId, e);
            return ApiResponse.error("회원 정지 해제에 실패했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/admin/{memberId}/delete")
    public ApiResponse<Void> deleteMember(@PathVariable Long memberId) {
        log.info("회원 삭제 요청 - memberId: {}", memberId);

        try {
            memberApplicationService.deleteMember(memberId);
            return ApiResponse.successVoid("회원이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("회원 삭제 실패 - memberId: {}", memberId, e);
            return ApiResponse.errorVoid("회원 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/admin/status/{status}")
    public ApiResponse<Page<MemberResponse>> getMembersByStatus(
            @PathVariable MemberStatus status,
            Pageable pageable) {

        log.info("상태별 회원 목록 조회 요청 - status: {}", status);

        try {
            Page<MemberResponse> members = memberApplicationService.getMembersByStatusAndRole(
                    status.name(), null, pageable);
            return ApiResponse.success(members);
        } catch (Exception e) {
            log.error("상태별 회원 목록 조회 실패 - status: {}", status, e);
            return ApiResponse.error("회원 목록을 가져올 수 없습니다.");
        }
    }

    @GetMapping("/admin/role/{role}")
    public ApiResponse<Page<MemberResponse>> getMembersByRole(
            @PathVariable Role role,
            Pageable pageable) {

        log.info("역할별 회원 목록 조회 요청 - role: {}", role);

        try {
            Page<MemberResponse> members = memberApplicationService.getMembersByStatusAndRole(
                    null, role.name(), pageable);
            return ApiResponse.success(members);
        } catch (Exception e) {
            log.error("역할별 회원 목록 조회 실패 - role: {}", role, e);
            return ApiResponse.error("회원 목록을 가져올 수 없습니다.");
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


    @PostMapping("/admin/{memberId}/role")
    public ApiResponse<MemberResponse> changeMemberRole(
            @PathVariable Long memberId,
            @RequestParam Role role,
            @CurrentUser AuthenticatedUser approver) {

        log.info("회원 역할 변경 요청 - memberId: {}, newRole: {}, approver: {}",
                memberId, role, approver.getUsername());

        try {
            MemberResponse response = memberApplicationService.changeMemberRole(memberId, role, approver.getId());
            return ApiResponse.success(response, "회원 역할이 변경되었습니다.");
        } catch (Exception e) {
            log.error("회원 역할 변경 실패 - memberId: {}, role: {}", memberId, role, e);
            return ApiResponse.error("회원 역할 변경에 실패했습니다: " + e.getMessage());
        }
    }


}