package com.antock.api.corpsearch.presentation;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpsearch.application.service.CorpMastSearchService;
import com.antock.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/corp")
@RequiredArgsConstructor
public class CorpMastSearchApiController {

    private final CorpMastSearchService corpMastService;

    @GetMapping("/search")
    public ApiResponse<Page<CorpMastSearchResponse>> search(
            @ModelAttribute CorpMastSearchRequest searchRequest) {

        log.debug("API 법인 검색 요청: {}", searchRequest);

        Page<CorpMastSearchResponse> result = corpMastService.search(searchRequest);

        return ApiResponse.of(HttpStatus.OK, result);
    }

    @GetMapping("/{id}")
    public ApiResponse<CorpMastSearchResponse> getById(@PathVariable Long id) {

        log.debug("API 법인 상세 조회 요청: ID = {}", id);

        CorpMastSearchResponse result = corpMastService.getById(id);

        return ApiResponse.of(HttpStatus.OK, result);
    }

    @GetMapping("/bizno/{bizNo}")
    public ApiResponse<CorpMastSearchResponse> getByBizNo(@PathVariable String bizNo) {

        log.debug("API 사업자번호 조회 요청: bizNo = {}", bizNo);

        CorpMastSearchResponse result = corpMastService.getByBizNo(bizNo);

        return ApiResponse.of(HttpStatus.OK, result);
    }

    @GetMapping("/regno/{corpRegNo}")
    public ApiResponse<CorpMastSearchResponse> getByCorpRegNo(@PathVariable String corpRegNo) {

        log.debug("API 법인등록번호 조회 요청: corpRegNo = {}", corpRegNo);

        CorpMastSearchResponse result = corpMastService.getByCorpRegNo(corpRegNo);

        return ApiResponse.of(HttpStatus.OK, result);
    }

    @GetMapping("/cities")
    public ApiResponse<List<String>> getCities() {

        log.debug("API 시/도 목록 조회 요청");

        List<String> cities = corpMastService.getAllCities();

        return ApiResponse.of(HttpStatus.OK, cities);
    }

    @GetMapping("/districts/{city}")
    public ApiResponse<List<String>> getDistrictsByCity(@PathVariable String city) {

        log.debug("API 구/군 목록 조회 요청: city = {}", city);

        List<String> districts = corpMastService.getDistrictsByCity(city);

        return ApiResponse.of(HttpStatus.OK, districts);
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getSearchStatistics(
            @ModelAttribute CorpMastSearchRequest searchRequest) {

        log.debug("API 검색 통계 조회 요청: {}", searchRequest);

        Map<String, Object> statistics = corpMastService.getSearchStatistics(searchRequest);

        return ApiResponse.of(HttpStatus.OK, statistics);
    }
}