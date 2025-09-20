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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
            jakarta.servlet.http.HttpServletRequest request) {

        String city = null;
        String district = null;

        try {
            log.info("Request URL: {}", request.getRequestURL());
            log.info("Query String: {}", request.getQueryString());

            city = request.getParameter("city");
            district = request.getParameter("district");

            log.info("Getting region details - original city: '{}', original district: '{}'", city, district);

            String decodedCity = city;
            String decodedDistrict = district;

            if (city != null && !city.trim().isEmpty()) {
                try {
                    decodedCity = java.net.URLDecoder.decode(city, "UTF-8");
                } catch (Exception e) {
                    log.warn("Failed to decode city parameter: '{}', using original", city);
                    decodedCity = city;
                }
            }

            if (district != null && !district.trim().isEmpty()) {
                try {
                    decodedDistrict = java.net.URLDecoder.decode(district, "UTF-8");
                } catch (Exception e) {
                    log.warn("Failed to decode district parameter: '{}', using original", district);
                    decodedDistrict = district;
                }
            }

            log.info("Getting region details - decoded city: '{}', decoded district: '{}'", decodedCity,
                    decodedDistrict);

            List<CorpMast> corpList;
            if (decodedCity != null && !decodedCity.trim().isEmpty() && decodedDistrict != null
                    && !decodedDistrict.trim().isEmpty()) {
                String trimmedCity = decodedCity.trim();
                String trimmedDistrict = decodedDistrict.trim();
                log.info("Searching for corps with city: '{}' and district: '{}'", trimmedCity, trimmedDistrict);
                corpList = corpMastRepository.findBySiNmAndSggNm(trimmedCity, trimmedDistrict);
                log.info("Found {} corps for city: '{}' and district: '{}'", corpList.size(), trimmedCity,
                        trimmedDistrict);

                if (corpList.isEmpty()) {
                    log.warn("No corps found. Let's check what's in the database...");
                    List<CorpMast> allCorps = corpMastRepository.findAll();
                    log.info("Total corps in database: {}", allCorps.size());

                    List<CorpMast> cityCorps = corpMastRepository.findBySiNm(trimmedCity);
                    log.info("Corps in city '{}': {}", trimmedCity, cityCorps.size());

                    if (!cityCorps.isEmpty()) {
                        log.info("Sample city corps: {}",
                                cityCorps.get(0).getSiNm() + " " + cityCorps.get(0).getSggNm());
                    }
                }
            } else if (decodedCity != null && !decodedCity.trim().isEmpty()) {
                corpList = corpMastRepository.findBySiNm(decodedCity.trim());
                log.info("Found {} corps for city: '{}'", corpList.size(), decodedCity);
            } else {
                log.warn("No city or district provided, returning empty list");
                corpList = List.of();
            }

            return ApiResponse.of(HttpStatus.OK, corpList);
        } catch (Exception e) {
            log.error("Error getting region details for city: '{}', district: '{}'", city, district, e);
            return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
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