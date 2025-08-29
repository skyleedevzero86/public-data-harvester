package com.antock.api.dashboard.presentation;

import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.dashboard.application.service.RegionStatService;
import com.antock.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/region-stats")
@RequiredArgsConstructor
@Tag(name = "Region Statistics", description = "지역별 통계 데이터 조회 API")
@SecurityRequirement(name = "Bearer Authentication")
public class RegionStatApiController {
    private final RegionStatService regionStatService;

    @Operation(summary = "최상위 지역 통계 조회", description = """
            가장 많은 법인 수를 보유한 지역의 통계 정보를 조회합니다.

            ### 반환 정보
            - 지역명 (시/도, 구/군)
            - 해당 지역의 총 법인 수
            - 전체 대비 비율
            - 지역 순위

            ### 활용 방안
            - 대시보드 메인 통계 표시
            - 주요 사업 지역 현황 파악
            """, tags = { "Region Statistics" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최상위 지역 통계 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionStatDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "통계 데이터를 찾을 수 없음", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/top")
    public com.antock.global.common.response.ApiResponse<RegionStatDto> getTopRegionStat() {
        return com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, regionStatService.getTopRegionStat());
    }

    @Operation(summary = "전체 지역 통계 목록 조회", description = """
            모든 지역의 통계 정보를 조회합니다.

            ### 반환 정보
            - 모든 지역(시/도, 구/군)의 통계 데이터
            - 각 지역별 법인 수
            - 전체 대비 비율
            - 지역별 순위

            ### 정렬 순서
            - 법인 수 내림차순으로 정렬
            - 동일 수인 경우 지역명 오름차순

            ### 활용 방안
            - 지역별 통계 차트 생성
            - 지역 비교 분석
            - 어드민 대시보드 표시
            """, tags = { "Region Statistics" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 지역 통계 목록 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping
    public com.antock.global.common.response.ApiResponse<List<RegionStatDto>> getAllRegionStats() {
        return com.antock.global.common.response.ApiResponse.of(HttpStatus.OK, regionStatService.getAllRegionStats());
    }
}