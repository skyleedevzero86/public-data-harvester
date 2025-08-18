package com.antock.api.corpmanual.presentation;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import com.antock.api.corpmanual.application.service.CorpMastManualExcelService;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
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
public class CorpMastManualApiController {

    private final CorpMastManualService corpMastService;
    private final CorpMastManualExcelService excelService;

    @GetMapping("/export")
    public void exportToExcel(
            CorpMastManualRequest request,
            HttpServletResponse response,
            @CurrentUser AuthenticatedUser user) throws Exception {

        log.info("엑셀 내보내기 요청: {}", request);

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"corp_data.xlsx\"");

        excelService.exportToExcel(request, response.getOutputStream());
    }

    @GetMapping("/search")
    public ApiResponse<Page<CorpMastManualResponse>> search(
            @ModelAttribute CorpMastManualRequest searchRequest) {

        log.info("법인 검색 요청: {}", searchRequest);
        Page<CorpMastManualResponse> result = corpMastService.search(searchRequest);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<CorpMastManualResponse> getById(@PathVariable Long id) {
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
    public ApiResponse<CorpMastManualResponse> getByBizNo(@PathVariable String bizNo) {
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
    public ApiResponse<CorpMastManualResponse> getByCorpRegNo(@PathVariable String corpRegNo) {
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
    public ApiResponse<List<String>> getDistrictsByCity(@PathVariable String city) {
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
    public ApiResponse<Map<String, Object>> getSearchStatistics(
            @ModelAttribute CorpMastManualRequest searchRequest) {

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