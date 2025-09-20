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
                    log.warn("No corps found with exact match. Let's check what's in the database...");

                    if (trimmedCity.equals(trimmedDistrict)) {
                        log.info("City and district are the same, trying to find by city only: '{}'", trimmedCity);
                        corpList = corpMastRepository.findBySiNm(trimmedCity);
                        log.info("Found {} corps for city only: '{}'", corpList.size(), trimmedCity);
                    }

                    if (corpList.isEmpty()) {
                        List<CorpMast> allCorps = corpMastRepository.findAll();
                        log.info("Total corps in database: {}", allCorps.size());

                        List<CorpMast> cityCorps = corpMastRepository.findBySiNm(trimmedCity);
                        log.info("Corps in city '{}': {}", trimmedCity, cityCorps.size());

                        if (!cityCorps.isEmpty()) {
                            log.info("Sample city corps: {}",
                                    cityCorps.get(0).getSiNm() + " " + cityCorps.get(0).getSggNm());
                        }
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