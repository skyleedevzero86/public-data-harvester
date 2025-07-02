package com.antock.api.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.value.Role;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 관리 API")
public class MemberApiController {

    private final MemberApplicationService memberApplicationService;

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다. 관리자 승인 후 로그인 가능합니다.")
    public ApiResponse<MemberResponse> join(
            @RequestBody @Valid MemberJoinRequest request) {

        MemberResponse response = memberApplicationService.join(request);
        return ApiResponse.of(HttpStatus.CREATED,
                "회원가입이 완료되었습니다. 관리자 승인 후 로그인이 가능합니다.", response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 토큰을 발급합니다.")
    public ApiResponse<MemberLoginResponse> login(
            @RequestBody @Valid MemberLoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIp(httpRequest);
        MemberLoginResponse response = memberApplicationService.login(request, clientIp);

        return ApiResponse.of(HttpStatus.OK, "로그인이 완료되었습니다.", response);
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberResponse> getMyInfo(@CurrentUser AuthenticatedUser user) {
        MemberResponse response = memberApplicationService.getMemberInfo(user.getId());
        return ApiResponse.of(HttpStatus.OK, response);
    }

    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MemberResponse> updateMyInfo(
            @CurrentUser AuthenticatedUser user,
            @RequestBody @Valid MemberUpdateRequest request) {

        MemberResponse response = memberApplicationService.updateProfile(user.getId(), request);
        return ApiResponse.of(HttpStatus.OK, "회원정보가 수정되었습니다.", response);
    }

    @GetMapping
    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<Page<MemberResponse>> getMembers(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<MemberResponse> response = memberApplicationService.getMembers(pageable);
        return ApiResponse.of(HttpStatus.OK, response);
    }

    @GetMapping("/pending")
    @Operation(summary = "승인 대기 회원 조회", description = "승인 대기중인 회원 목록을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<Page<MemberResponse>> getPendingMembers(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<MemberResponse> response = memberApplicationService.getPendingMembers(pageable);
        return ApiResponse.of(HttpStatus.OK, response);
    }

    @PostMapping("/{memberId}/approve")
    @Operation(summary = "회원 승인", description = "대기중인 회원을 승인합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<MemberResponse> approveMember(
            @PathVariable Long memberId,
            @CurrentUser AuthenticatedUser user) {

        MemberResponse response = memberApplicationService.approveMember(memberId, user.getId());
        return ApiResponse.of(HttpStatus.OK, "회원이 승인되었습니다.", response);
    }

    @PostMapping("/{memberId}/reject")
    @Operation(summary = "회원 거부", description = "대기중인 회원을 거부합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<MemberResponse> rejectMember(@PathVariable Long memberId) {
        MemberResponse response = memberApplicationService.rejectMember(memberId);
        return ApiResponse.of(HttpStatus.OK, "회원이 거부되었습니다.", response);
    }

    @PostMapping("/{memberId}/suspend")
    @Operation(summary = "회원 정지", description = "회원을 정지시킵니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MemberResponse> suspendMember(@PathVariable Long memberId) {
        MemberResponse response = memberApplicationService.suspendMember(memberId);
        return ApiResponse.of(HttpStatus.OK, "회원이 정지되었습니다.", response);
    }

    @PutMapping("/{memberId}/role")
    @Operation(summary = "권한 변경", description = "회원의 권한을 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MemberResponse> changeRole(
            @PathVariable Long memberId,
            @RequestParam Role role) {

        MemberResponse response = memberApplicationService.changeRole(memberId, role);
        return ApiResponse.of(HttpStatus.OK, "권한이 변경되었습니다.", response);
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