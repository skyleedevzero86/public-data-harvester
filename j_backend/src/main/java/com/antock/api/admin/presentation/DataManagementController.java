package com.antock.api.admin.presentation;

import com.antock.api.member.infrastructure.MemberRepository;
import com.antock.api.member.value.MemberStatus;
import com.antock.api.member.value.Role;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.config.DataInitProperties;
import com.antock.global.config.MemberDataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Admin - Data Management", description = "데이터 관리 API (개발/테스트 환경 전용)")
@Slf4j
@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Profile({"dev", "local", "test"})
public class DataManagementController {

    private final MemberRepository memberRepository;
    private final MemberDataGenerator memberDataGenerator;
    private final DataInitProperties dataInitProperties;

    @Operation(summary = "회원 데이터 통계 조회", description = "현재 시스템의 회원 데이터 통계를 역할별, 상태별로 조회합니다.")
    @GetMapping("/members/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMemberStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        statistics.put("totalCount", memberRepository.count());

        Map<String, Object> roleStats = new HashMap<>();
        for (Role role : Role.values()) {
            long count = memberRepository.countByRole(role);
            roleStats.put(role.name(), Map.of(
                    "count", count,
                    "description", role.getDescription()
            ));
        }
        statistics.put("byRole", roleStats);

        Map<String, Object> statusStats = new HashMap<>();
        for (MemberStatus status : MemberStatus.values()) {
            long count = memberRepository.countByStatus(status);
            statusStats.put(status.name(), Map.of(
                    "count", count,
                    "description", status.getDescription()
            ));
        }
        statistics.put("byStatus", statusStats);

        Map<String, Object> detailedStats = new HashMap<>();
        for (Role role : Role.values()) {
            Map<String, Object> roleDetail = new HashMap<>();
            for (MemberStatus status : MemberStatus.values()) {
                long count = memberRepository.countByRoleAndStatus(role, status);
                if (count > 0) {
                    roleDetail.put(status.name(), count);
                }
            }
            if (!roleDetail.isEmpty()) {
                detailedStats.put(role.name(), roleDetail);
            }
        }
        statistics.put("detailed", detailedStats);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @Operation(summary = "더미 회원 데이터 생성", description = "지정된 수만큼의 더미 회원 데이터를 생성합니다. 기존 데이터가 있으면 추가로 생성됩니다.")
    @PostMapping("/members/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateMemberData(
            @Parameter(description = "생성할 회원 수", example = "1000")
            @RequestParam(defaultValue = "1000") int count) {

        if (count <= 0 || count > 10000) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("생성할 회원 수는 1~10000 사이여야 합니다.")
            );
        }

        log.info("관리자 요청으로 {} 명의 더미 회원 데이터 생성 시작", count);

        long beforeCount = memberRepository.count();

        try {
            boolean originalForceInit = dataInitProperties.isForceInit();
            dataInitProperties.setForceInit(true);

            memberDataGenerator.generateMembers(count);

            dataInitProperties.setForceInit(originalForceInit);

            long afterCount = memberRepository.count();
            long generatedCount = afterCount - beforeCount;

            Map<String, Object> result = new HashMap<>();
            result.put("beforeCount", beforeCount);
            result.put("afterCount", afterCount);
            result.put("generatedCount", generatedCount);
            result.put("requestedCount", count);
            result.put("success", true);

            log.info("더미 회원 데이터 생성 완료: {} 명 생성됨", generatedCount);

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            log.error("더미 회원 데이터 생성 실패", e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("더미 데이터 생성 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    @Operation(summary = "데이터 초기화 설정 조회", description = "현재 데이터 초기화 관련 설정값들을 조회합니다.")
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDataConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", dataInitProperties.isEnabled());
        config.put("memberCount", dataInitProperties.getMemberCount());
        config.put("forceInit", dataInitProperties.isForceInit());
        config.put("batchSize", dataInitProperties.getBatchSize());

        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @Operation(summary = "데이터 초기화 설정 업데이트", description = "데이터 초기화 관련 설정값들을 업데이트합니다. (런타임 변경)")
    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateDataConfig(
            @Parameter(description = "초기화 활성화 여부") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "생성할 회원 수") @RequestParam(required = false) Integer memberCount,
            @Parameter(description = "강제 초기화 여부") @RequestParam(required = false) Boolean forceInit,
            @Parameter(description = "배치 크기") @RequestParam(required = false) Integer batchSize) {

        if (enabled != null) {
            dataInitProperties.setEnabled(enabled);
        }
        if (memberCount != null && memberCount > 0 && memberCount <= 10000) {
            dataInitProperties.setMemberCount(memberCount);
        }
        if (forceInit != null) {
            dataInitProperties.setForceInit(forceInit);
        }
        if (batchSize != null && batchSize > 0 && batchSize <= 1000) {
            dataInitProperties.setBatchSize(batchSize);
        }

        Map<String, Object> updatedConfig = new HashMap<>();
        updatedConfig.put("enabled", dataInitProperties.isEnabled());
        updatedConfig.put("memberCount", dataInitProperties.getMemberCount());
        updatedConfig.put("forceInit", dataInitProperties.isForceInit());
        updatedConfig.put("batchSize", dataInitProperties.getBatchSize());

        log.info("데이터 초기화 설정이 업데이트되었습니다: {}", updatedConfig);

        return ResponseEntity.ok(ApiResponse.success(updatedConfig));
    }
}