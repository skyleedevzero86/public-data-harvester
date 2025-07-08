package com.antock.api.corpsearch.presentation;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpsearch.application.service.CorpMastSearchService;
import com.antock.api.corpsearch.application.service.CorpExcelService;
import com.antock.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/corp")
@RequiredArgsConstructor
public class CorpMastSearchApiController {

    private final CorpMastSearchService corpMastService;
    private final CorpExcelService corpExcelService;

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

    @GetMapping("/excel/download")
    public ResponseEntity<byte[]> downloadExcel(
            @ModelAttribute CorpMastSearchRequest searchRequest) {

        log.debug("API Excel 다운로드 요청: {}", searchRequest);

        try {
            byte[] excelData = corpExcelService.generateExcel(searchRequest);

            String fileName = corpExcelService.generateFileName();
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.setContentLength(excelData.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            log.debug("Excel 다운로드 성공: {} bytes, 파일명: {}", excelData.length, fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (IllegalArgumentException e) {
            log.warn("Excel 다운로드 요청 검증 실패: {}", e.getMessage());

            String errorMessage = String.format("{\"error\": \"%s\"}", e.getMessage());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity.badRequest()
                    .headers(headers)
                    .body(errorMessage.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            log.error("Excel 파일 생성 중 오류 발생", e);

            String errorMessage = "{\"error\": \"Excel 파일 생성 중 오류가 발생했습니다.\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(errorMessage.getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/excel/info")
    public ApiResponse<Map<String, Object>> getExcelDownloadInfo(
            @ModelAttribute CorpMastSearchRequest searchRequest) {

        log.debug("Excel 다운로드 정보 조회 요청: {}", searchRequest);

        try {
            Page<CorpMastSearchResponse> searchResult = corpMastService.search(searchRequest);
            long totalCount = searchResult.getTotalElements();

            int maxRecords = corpExcelService.getMaxExcelRecords();
            boolean downloadable = searchRequest.hasSearchCondition() && totalCount <= maxRecords;

            Map<String, Object> info = Map.of(
                    "totalCount", totalCount,
                    "maxRecords", maxRecords,
                    "downloadable", downloadable,
                    "hasSearchCondition", searchRequest.hasSearchCondition(),
                    "message", downloadable ? "다운로드 가능합니다." :
                            !searchRequest.hasSearchCondition() ? "검색 조건을 입력해주세요." :
                                    String.format("최대 %,d건까지만 다운로드 가능합니다. (검색결과: %,d건)", maxRecords, totalCount)
            );

            return ApiResponse.of(HttpStatus.OK, info);

        } catch (Exception e) {
            log.error("Excel 다운로드 정보 조회 중 오류 발생", e);

            Map<String, Object> errorInfo = Map.of(
                    "downloadable", false,
                    "message", "다운로드 정보 조회 중 오류가 발생했습니다."
            );

            return ApiResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, errorInfo);
        }
    }
}