package com.antock.api.member.presentation;

import com.antock.api.member.application.dto.request.MemberJoinRequest;
import com.antock.api.member.application.dto.request.MemberLoginRequest;
import com.antock.api.member.application.dto.request.MemberPasswordChangeRequest;
import com.antock.api.member.application.dto.request.MemberUpdateRequest;
import com.antock.api.member.application.dto.response.MemberLoginResponse;
import com.antock.api.member.application.dto.response.MemberResponse;
import com.antock.api.member.application.dto.response.MemberStatsDto;
import com.antock.api.member.application.dto.response.PasswordStatusResponse;
import com.antock.api.member.application.service.MemberApplicationService;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "회원 관리 및 인증 API")
public class MemberApiController {

    private final MemberApplicationService memberApplicationService;

    @PostMapping("/join")
    @Operation(
        summary = "회원가입",
        description = "새로운 회원을 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "중복된 사용자명 또는 이메일")
    })
    public ApiResponse<MemberResponse> join(
            @Parameter(description = "회원가입 정보") @Valid @RequestBody MemberJoinRequest request) {
        log.info("회원가입 요청: username={}", request.getUsername());
        
        try {
            MemberResponse result = memberApplicationService.join(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원가입에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "사용자 인증을 수행하고 JWT 토큰을 발급합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberLoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "423", description = "계정 잠김")
    })
    public ApiResponse<MemberLoginResponse> login(
            @Parameter(description = "로그인 정보") @Valid @RequestBody MemberLoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("로그인 시도: username={}", request.getUsername());
        
        try {
            String clientIp = getClientIpAddress(httpRequest);
            MemberLoginResponse result = memberApplicationService.login(request, clientIp);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage(), e);
            return ApiResponse.error("로그인에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/profile")
    @Operation(
        summary = "내 프로필 조회",
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ApiResponse<MemberResponse> getMyProfile(@CurrentUser AuthenticatedUser user) {
        log.info("프로필 조회 요청: userId={}", user.getId());
        
        try {
            MemberResponse result = memberApplicationService.getCurrentMemberInfo(user.getId());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("프로필 조회 실패: {}", e.getMessage(), e);
            return ApiResponse.error("프로필 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    @Operation(
        summary = "프로필 수정",
        description = "현재 로그인한 사용자의 프로필 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "409", description = "중복된 이메일")
    })
    public ApiResponse<MemberResponse> updateProfile(
            @Parameter(description = "프로필 수정 정보") @Valid @RequestBody MemberUpdateRequest request,
            @CurrentUser AuthenticatedUser user) {
        log.info("프로필 수정 요청: userId={}", user.getId());
        
        try {
            MemberResponse result = memberApplicationService.updateProfile(user.getId(), request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("프로필 수정 실패: {}", e.getMessage(), e);
            return ApiResponse.error("프로필 수정에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/password/change")
    @Operation(
        summary = "비밀번호 변경",
        description = "현재 로그인한 사용자의 비밀번호를 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ApiResponse<Void> changePassword(
            @Parameter(description = "비밀번호 변경 정보") @Valid @RequestBody MemberPasswordChangeRequest request,
            @CurrentUser AuthenticatedUser user) {
        log.info("비밀번호 변경 요청: userId={}", user.getId());
        
        try {
            memberApplicationService.changePassword(user.getId(), request);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage(), e);
            return ApiResponse.error("비밀번호 변경에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/password/status")
    @Operation(
        summary = "비밀번호 상태 조회",
        description = "현재 로그인한 사용자의 비밀번호 상태를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = PasswordStatusResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ApiResponse<PasswordStatusResponse> getPasswordStatus(@CurrentUser AuthenticatedUser user) {
        log.info("비밀번호 상태 조회 요청: userId={}", user.getId());
        
        try {
            PasswordStatusResponse result = memberApplicationService.getPasswordStatus(user.getId());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("비밀번호 상태 조회 실패: {}", e.getMessage(), e);
            return ApiResponse.error("비밀번호 상태 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    @Operation(
        summary = "회원 목록 조회 (관리자)",
        description = "관리자가 전체 회원 목록을 조회합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ApiResponse<Page<MemberResponse>> getMembers(
            @Parameter(description = "페이지 정보") Pageable pageable) {
        log.info("회원 목록 조회 요청");
        
        try {
            Page<MemberResponse> result = memberApplicationService.getMembers(pageable);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원 목록 조회 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/admin/pending")
    @Operation(
        summary = "승인 대기 회원 목록 (관리자)",
        description = "관리자가 승인 대기 중인 회원 목록을 조회합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ApiResponse<Page<MemberResponse>> getPendingMembers(
            @Parameter(description = "페이지 정보") Pageable pageable) {
        log.info("승인 대기 회원 목록 조회 요청");
        
        try {
            Page<MemberResponse> result = memberApplicationService.getPendingMembers(pageable);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("승인 대기 회원 목록 조회 실패: {}", e.getMessage(), e);
            return ApiResponse.error("승인 대기 회원 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/approve")
    @Operation(
        summary = "회원 승인 (관리자)",
        description = "관리자가 승인 대기 중인 회원을 승인합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ApiResponse<MemberResponse> approveMember(
            @Parameter(description = "승인할 회원 ID", example = "123") @PathVariable Long memberId,
            @CurrentUser AuthenticatedUser approver) {
        log.info("회원 승인 요청: memberId={}, approverId={}", memberId, approver.getId());
        
        try {
            MemberResponse result = memberApplicationService.approveMember(memberId, approver.getId());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원 승인 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원 승인에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/reject")
    @Operation(
        summary = "회원 거부 (관리자)",
        description = "관리자가 승인 대기 중인 회원을 거부합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ApiResponse<MemberResponse> rejectMember(
            @Parameter(description = "거부할 회원 ID", example = "123") @PathVariable Long memberId) {
        log.info("회원 거부 요청: memberId={}", memberId);
        
        try {
            MemberResponse result = memberApplicationService.rejectMember(memberId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원 거부 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원 거부에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/suspend")
    @Operation(
        summary = "회원 정지 (관리자)",
        description = "관리자가 회원을 정지시킵니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ApiResponse<MemberResponse> suspendMember(
            @Parameter(description = "정지할 회원 ID", example = "123") @PathVariable Long memberId) {
        log.info("회원 정지 요청: memberId={}", memberId);
        
        try {
            MemberResponse result = memberApplicationService.suspendMember(memberId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원 정지 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원 정지에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/unlock")
    @Operation(
        summary = "회원 정지 해제 (관리자)",
        description = "관리자가 정지된 회원의 정지를 해제합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ApiResponse<MemberResponse> unlockMember(
            @Parameter(description = "정지 해제할 회원 ID", example = "123") @PathVariable Long memberId) {
        log.info("회원 정지 해제 요청: memberId={}", memberId);
        
        try {
            MemberResponse result = memberApplicationService.unlockMember(memberId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원 정지 해제 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원 정지 해제에 실패했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/admin/{memberId}/role")
    @Operation(
        summary = "회원 역할 변경 (관리자)",
        description = "관리자가 회원의 역할을 변경합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = MemberResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    public ApiResponse<MemberResponse> changeRole(
            @Parameter(description = "역할을 변경할 회원 ID", example = "123") @PathVariable Long memberId,
            @Parameter(description = "새로운 역할", example = "MANAGER") @RequestParam Role role) {
        log.info("회원 역할 변경 요청: memberId={}, newRole={}", memberId, role);
        
        try {
            MemberResponse result = memberApplicationService.changeRole(memberId, role);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원 역할 변경 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원 역할 변경에 실패했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/admin/stats")
    @Operation(
        summary = "회원 통계 조회 (관리자)",
        description = "관리자가 회원 관련 통계 정보를 조회합니다."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공", 
            content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ApiResponse<List<MemberStatsDto>> getMemberStats() {
        log.info("회원 통계 조회 요청");
        
        try {
            List<MemberStatsDto> result = memberApplicationService.getMemberStats();
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("회원 통계 조회 실패: {}", e.getMessage(), e);
            return ApiResponse.error("회원 통계 조회에 실패했습니다: " + e.getMessage());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
}