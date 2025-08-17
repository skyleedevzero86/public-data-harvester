package com.antock.api.corpmanual.presentation;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualExcelService;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import com.antock.api.global.common.response.ApiResponse;
import com.antock.api.global.security.annotation.CurrentUser;
import com.antock.api.global.security.dto.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/corp")
@RequiredArgsConstructor
@Tag(name = "Corp - Manual Management", description = "법인 정보 수동 관리 API")
public class CorpMastManualApiController {

    private final CorpMastManualService corpMastService;
    private final CorpMastManualExcelService excelService;

    @GetMapping("/export")
    @Operation(summary = "법인 정보 엑셀 내보내기", description = "검색 조건에 맞는 법인 정보를 엑셀 파일로 내보냅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public void exportToExcel(
            @Parameter(description = "검색 조건") CorpMastManualRequest request,
            HttpServletResponse response,
            @CurrentUser AuthenticatedUser user) throws Exception {

        log.info("엑셀 내보내기 요청: {}", request);

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"corp_data.xlsx\"");

        excelService.exportToExcel(request, response.getOutputStream());
    }

    @GetMapping("/search")
    @Operation(summary = "법인 정보 검색", description = "다양한 조건으로 법인 정보를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<Page<CorpMastManualResponse>> search(
            @Parameter(description = "검색 조건") @ModelAttribute CorpMastManualRequest searchRequest) {

        log.info("법인 검색 요청: {}", searchRequest);
        Page<CorpMastManualResponse> result = corpMastService.search(searchRequest);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "법인 정보 상세 조회", description = "ID로 특정 법인의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = CorpMastManualResponse.class))),
            @ApiResponse(responseCode = "404", description = "법인 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<CorpMastManualResponse> getById(
            @Parameter(description = "법인 ID", example = "123") @PathVariable Long id) {
        log.info("법인 정보 조회 요청 - ID: {}", id);

        try {
            CorpMastManualResponse result = corpMastService.getById(id);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("법인 정보 조회 실패 - ID: {}", id, e);
            return ApiResponse.error("법인 정보를 찾을 수 없습니다.");
        }
    }

    @GetMapping("/bizno/{bizNo}")
    @Operation(summary = "사업자번호로 법인 정보 조회", description = "사업자번호로 법인 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = CorpMastManualResponse.class))),
            @ApiResponse(responseCode = "404", description = "법인 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<CorpMastManualResponse> getByBizNo(
            @Parameter(description = "사업자번호", example = "123-45-67890") @PathVariable String bizNo) {
        log.info("사업자번호로 법인 정보 조회 요청: {}", bizNo);

        try {
            CorpMastManualResponse result = corpMastService.getByBizNo(bizNo);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("사업자번호로 법인 정보 조회 실패: {}", bizNo, e);
            return ApiResponse.error("해당 사업자번호의 법인 정보를 찾을 수 없습니다.");
        }
    }

    @GetMapping("/regno/{corpRegNo}")
    @Operation(summary = "법인등록번호로 법인 정보 조회", description = "법인등록번호로 법인 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = CorpMastManualResponse.class))),
            @ApiResponse(responseCode = "404", description = "법인 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<CorpMastManualResponse> getByCorpRegNo(
            @Parameter(description = "법인등록번호", example = "110111-1234567") @PathVariable String corpRegNo) {
        log.info("법인등록번호로 법인 정보 조회 요청: {}", corpRegNo);

        try {
            CorpMastManualResponse result = corpMastService.getByCorpRegNo(corpRegNo);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("법인등록번호로 법인 정보 조회 실패: {}", corpRegNo, e);
            return ApiResponse.error("해당 법인등록번호의 법인 정보를 찾을 수 없습니다.");
        }
    }

    @GetMapping("/cities")
    @Operation(summary = "도시 목록 조회", description = "등록된 모든 도시 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<List<String>> getCities() {
        log.info("도시 목록 조회 요청");

        try {
            List<String> cities = corpMastService.getAllCities();
            return ApiResponse.success(cities);
        } catch (Exception e) {
            log.error("도시 목록 조회 실패", e);
            return ApiResponse.error("도시 목록을 가져올 수 없습니다.");
        }
    }

    @GetMapping("/districts/{city}")
    @Operation(summary = "구/군 목록 조회", description = "지정된 도시의 구/군 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 도시명"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<List<String>> getDistrictsByCity(
            @Parameter(description = "도시명", example = "서울특별시") @PathVariable String city) {
        log.info("구/군 목록 조회 요청 - 도시: {}", city);

        try {
            List<String> districts = corpMastService.getDistrictsByCity(city);
            return ApiResponse.success(districts);
        } catch (Exception e) {
            log.error("구/군 목록 조회 실패 - 도시: {}", city, e);
            return ApiResponse.error("해당 도시의 구/군 목록을 가져올 수 없습니다.");
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "검색 통계 조회", description = "검색 조건에 따른 통계 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ApiResponse<Map<String, Object>> getSearchStatistics(
            @Parameter(description = "검색 조건") @ModelAttribute CorpMastManualRequest searchRequest) {

        log.info("검색 통계 요청: {}", searchRequest);

        try {
            Map<String, Object> statistics = corpMastService.getSearchStatistics(searchRequest);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("검색 통계 조회 실패", e);
            return ApiResponse.error("검색 통계를 가져올 수 없습니다.");
        }
    }
}