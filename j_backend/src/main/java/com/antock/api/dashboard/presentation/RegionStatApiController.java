package com.antock.api.dashboard.presentation;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.dashboard.application.service.RegionStatService;
import com.antock.global.common.response.ApiResponse;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/region-stats")
@RequiredArgsConstructor
public class RegionStatApiController {
    private final RegionStatService regionStatService;
    private final CorpMastRepository corpMastRepository;

    @GetMapping("/top")
    public ApiResponse<RegionStatDto> getTopRegionStat() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getTopRegionStat());
    }

    @GetMapping
    public ApiResponse<List<RegionStatDto>> getAllRegionStats() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getAllRegionStats());
    }

    @GetMapping("/paged")
    public ApiResponse<Page<RegionStatDto>> getRegionStatsWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("completionRate").descending());
        Page<RegionStatDto> result = regionStatService.getRegionStatsWithPaging(pageable, city, district);
        return ApiResponse.of(HttpStatus.OK, result);
    }

    @GetMapping("/cities")
    public ApiResponse<List<String>> getCities() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getCities());
    }

    @GetMapping("/districts")
    public ApiResponse<List<String>> getDistricts(
            @RequestParam String city) {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getDistrictsByCity(city));
    }

    @GetMapping("/details")
    public ApiResponse<Page<CorpMast>> getRegionDetails(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "18") int size) {
        try {
            String decodedCity = city;
            String decodedDistrict = district;
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
            Page<CorpMast> corpPage;
            if (decodedCity != null && !decodedCity.trim().isEmpty() && decodedDistrict != null
                    && !decodedDistrict.trim().isEmpty()) {
                String trimmedCity = decodedCity.trim();
                String trimmedDistrict = decodedDistrict.trim();
                corpPage = corpMastRepository.findBySiNmAndSggNm(trimmedCity, trimmedDistrict, pageable);
                if (corpPage.isEmpty() && trimmedCity.equals(trimmedDistrict)) {
                    corpPage = corpMastRepository.findBySiNm(trimmedCity, pageable);
                }
            } else if (decodedCity != null && !decodedCity.trim().isEmpty()) {
                corpPage = corpMastRepository.findBySiNm(decodedCity.trim(), pageable);
            } else {
                corpPage = corpMastRepository.findAll(pageable);
            }
            return ApiResponse.of(HttpStatus.OK, corpPage);
        } catch (Exception e) {
            return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Deprecated(since = "2024-01-15", forRemoval = true)
    @PostMapping("/admin/add-missing-columns")
    public ApiResponse<Map<String, Object>> addMissingColumns() {
        return ApiResponse.of(HttpStatus.GONE, Map.of(
                "error", "UnsupportedOperationException",
                "message", "런타임 DDL 실행은 지원되지 않습니다. 데이터베이스 스키마 변경은 Flyway 마이그레이션 스크립트를 통해 관리해야 합니다.",
                "migration_script", "src/main/resources/db/migration/V2__Add_corp_mast_additional_columns.sql",
                "deprecated", true));
    }

    @Deprecated(since = "2024-01-15", forRemoval = true)
    @PostMapping("/admin/add-sample-data")
    public ApiResponse<Map<String, Object>> addSampleData() {
        return ApiResponse.of(HttpStatus.GONE, Map.of(
                "error", "UnsupportedOperationException",
                "message", "런타임 데이터 변경은 지원되지 않습니다. 샘플 데이터는 Flyway 마이그레이션 스크립트를 통해 관리해야 합니다.",
                "migration_script", "src/main/resources/db/migration/V3__Add_sample_data_for_corp_mast.sql",
                "deprecated", true));
    }

    @GetMapping("/debug/fields")
    public ApiResponse<Map<String, Object>> debugFields(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        try {
            List<CorpMast> corpList;
            if (city != null && district != null) {
                corpList = corpMastRepository.findBySiNmAndSggNm(city, district);
            } else {
                corpList = corpMastRepository.findAll();
            }
            if (corpList.isEmpty()) {
                return ApiResponse.of(HttpStatus.OK, Map.of(
                        "message", "No data found",
                        "city", city,
                        "district", district,
                        "sample_data", "No corporations found"));
            }
            CorpMast sample = corpList.get(0);
            Map<String, Object> debugInfo = Map.of(
                    "total_count", corpList.size(),
                    "city", city,
                    "district", district,
                    "sample_corp", Map.of(
                            "id", sample.getId(),
                            "bizNm", sample.getBizNm(),
                            "repNm", sample.getRepNm(),
                            "estbDt", sample.getEstbDt(),
                            "roadNmAddr", sample.getRoadNmAddr(),
                            "jibunAddr", sample.getJibunAddr(),
                            "corpStatus", sample.getCorpStatus(),
                            "siNm", sample.getSiNm(),
                            "sggNm", sample.getSggNm()),
                    "field_analysis", Map.of(
                            "has_rep_nm", sample.getRepNm() != null && !sample.getRepNm().isEmpty(),
                            "has_estb_dt", sample.getEstbDt() != null && !sample.getEstbDt().isEmpty(),
                            "has_road_addr", sample.getRoadNmAddr() != null && !sample.getRoadNmAddr().isEmpty(),
                            "has_jibun_addr", sample.getJibunAddr() != null && !sample.getJibunAddr().isEmpty(),
                            "has_corp_status", sample.getCorpStatus() != null && !sample.getCorpStatus().isEmpty()));
            return ApiResponse.of(HttpStatus.OK, debugInfo);
        } catch (Exception e) {
            return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, Map.of(
                    "error", e.getMessage(),
                    "city", city,
                    "district", district));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
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