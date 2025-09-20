package com.antock.api.dashboard.presentation;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.dashboard.application.service.RegionStatService;
import com.antock.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/region-stats")
@RequiredArgsConstructor
@Tag(name = "Region Statistics", description = "지역별 통계 데이터 조회 API")
public class RegionStatApiController {
    private final RegionStatService regionStatService;
    private final CorpMastRepository corpMastRepository;

    @Operation(summary = "최상위 지역 통계 조회", description = "가장 많은 법인 수를 보유한 지역의 통계 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "최상위 지역 통계 조회 성공")
    @GetMapping("/top")
    public ApiResponse<RegionStatDto> getTopRegionStat() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getTopRegionStat());
    }

    @Operation(summary = "전체 지역 통계 목록 조회", description = "모든 지역의 통계 정보를 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 지역 통계 목록 조회 성공")
    @GetMapping
    public ApiResponse<List<RegionStatDto>> getAllRegionStats() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getAllRegionStats());
    }

    @Operation(summary = "지역별 통계 페이징 조회", description = "지역별 통계를 페이징하여 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "지역별 통계 페이징 조회 성공")
    @GetMapping("/paged")
    public ApiResponse<Page<RegionStatDto>> getRegionStatsWithPaging(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "25") int size,
            @Parameter(description = "시/도") @RequestParam(required = false) String city,
            @Parameter(description = "구/군") @RequestParam(required = false) String district) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("completionRate").descending());
        Page<RegionStatDto> result = regionStatService.getRegionStatsWithPaging(pageable, city, district);
        return ApiResponse.of(HttpStatus.OK, result);
    }

    @Operation(summary = "시/도 목록 조회", description = "등록된 시/도 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시/도 목록 조회 성공")
    @GetMapping("/cities")
    public ApiResponse<List<String>> getCities() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getCities());
    }

    @Operation(summary = "구/군 목록 조회", description = "특정 시/도의 구/군 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "구/군 목록 조회 성공")
    @GetMapping("/districts")
    public ApiResponse<List<String>> getDistricts(
            @Parameter(description = "시/도") @RequestParam String city) {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getDistrictsByCity(city));
    }

    @Operation(summary = "지역별 상세 법인 목록 조회", description = "특정 지역의 법인 목록을 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상세 법인 목록 조회 성공")
    @GetMapping("/details")
    public ApiResponse<List<CorpMast>> getRegionDetails(
            @Parameter(description = "시/도") @RequestParam String city,
            @Parameter(description = "구/군") @RequestParam String district) {

        List<CorpMast> corpList;
        if (city != null && !city.isEmpty() && district != null && !district.isEmpty()) {
            corpList = corpMastRepository.findBySiNmAndSggNm(city, district);
        } else if (city != null && !city.isEmpty()) {
            corpList = corpMastRepository.findBySiNm(city);
        } else {
            corpList = corpMastRepository.findAll();
        }

        return ApiResponse.of(HttpStatus.OK, corpList);
    }

    @Operation(summary = "Excel 다운로드", description = "지역별 통계와 상세 법인 목록을 Excel 파일로 다운로드합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Excel 다운로드 성공")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @Parameter(description = "시/도") @RequestParam(required = false) String city,
            @Parameter(description = "구/군") @RequestParam(required = false) String district) {

        try {
            byte[] excelData = regionStatService.exportToExcel(city, district);

            String fileName = "지역별통계_";
            if (city != null && !city.isEmpty()) {
                fileName += city;
                if (district != null && !district.isEmpty()) {
                    fileName += "_" + district;
                }
            } else {
                fileName += "전체";
            }
            fileName += "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName)
                    .header("Content-Length", String.valueOf(excelData.length))
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}