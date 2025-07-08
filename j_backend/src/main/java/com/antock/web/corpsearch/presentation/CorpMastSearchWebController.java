package com.antock.web.corpsearch.presentation;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpsearch.application.service.CorpMastSearchService;
import com.antock.api.corpsearch.application.service.CorpExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/corp")
@RequiredArgsConstructor
public class CorpMastSearchWebController {

    private final CorpMastSearchService corpMastService;
    private final CorpExcelService corpExcelService;

    @GetMapping("/search")
    public String searchPage(
            @ModelAttribute CorpMastSearchRequest searchRequest,
            Model model) {

        log.debug("법인 검색 페이지 요청: {}", searchRequest);

        Page<CorpMastSearchResponse> corpList = null;
        Map<String, Object> statistics = null;

        if (searchRequest.hasSearchCondition()) {
            log.debug("검색 조건 존재, 검색 실행");
            try {
                corpList = corpMastService.search(searchRequest);
                statistics = corpMastService.getSearchStatistics(searchRequest);

                log.debug("검색 결과: 총 {}건", corpList != null ? corpList.getTotalElements() : 0);
            } catch (Exception e) {
                log.error("검색 실행 중 오류 발생", e);
                model.addAttribute("errorMessage", "검색 중 오류가 발생했습니다. 다시 시도해주세요.");
            }
        } else {
            log.debug("검색 조건 없음, 검색 미실행");
        }

        List<String> cities = corpMastService.getAllCities();

        List<String> districts = null;
        if (searchRequest.getCity() != null && !searchRequest.getCity().trim().isEmpty()) {
            districts = corpMastService.getDistrictsByCity(searchRequest.getCity());
        }

        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("corpList", corpList);
        model.addAttribute("statistics", statistics);
        model.addAttribute("cities", cities);
        model.addAttribute("districts", districts);

        return "corp/search";
    }

    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        log.debug("법인 상세 페이지 요청: ID = {}", id);

        try {
            CorpMastSearchResponse corp = corpMastService.getById(id);
            model.addAttribute("corp", corp);

            return "corp/detail";

        } catch (Exception e) {
            log.error("법인 상세 조회 실패: ID = {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "법인 정보를 찾을 수 없습니다.");
            return "redirect:/corp/search";
        }
    }

    @GetMapping("/districts/{city}")
    @ResponseBody
    public List<String> getDistrictsByCity(@PathVariable String city) {
        log.debug("구/군 목록 조회 요청: city = {}", city);
        try {
            return corpMastService.getDistrictsByCity(city);
        } catch (Exception e) {
            log.error("구/군 목록 조회 실패: city = {}", city, e);
            return List.of();
        }
    }

    @PostMapping("/search/reset")
    public String resetSearch(RedirectAttributes redirectAttributes) {
        log.debug("검색 조건 초기화 요청");
        redirectAttributes.addFlashAttribute("message", "검색 조건이 초기화되었습니다.");
        return "redirect:/corp/search";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @ModelAttribute CorpMastSearchRequest searchRequest,
            RedirectAttributes redirectAttributes) {

        log.debug("웹 Excel 다운로드 요청: {}", searchRequest);

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

            log.debug("웹 Excel 다운로드 성공: {} bytes, 파일명: {}", excelData.length, fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (IllegalArgumentException e) {
            log.warn("Excel 다운로드 요청 검증 실패: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            try {
                String queryString = buildQueryString(searchRequest);
                return ResponseEntity.status(302)
                        .header("Location", "/corp/search?" + queryString)
                        .build();
            } catch (Exception redirectError) {
                log.error("리다이렉트 중 오류 발생", redirectError);
                return ResponseEntity.status(302)
                        .header("Location", "/corp/search")
                        .build();
            }

        } catch (IOException e) {
            log.error("Excel 파일 생성 중 오류 발생", e);

            redirectAttributes.addFlashAttribute("errorMessage", "Excel 파일 생성 중 오류가 발생했습니다.");

            try {
                String queryString = buildQueryString(searchRequest);
                return ResponseEntity.status(302)
                        .header("Location", "/corp/search?" + queryString)
                        .build();
            } catch (Exception redirectError) {
                log.error("리다이렉트 중 오류 발생", redirectError);
                return ResponseEntity.status(302)
                        .header("Location", "/corp/search")
                        .build();
            }
        }
    }

    private String buildQueryString(CorpMastSearchRequest request) {
        StringBuilder queryString = new StringBuilder();

        try {
            if (isNotEmpty(request.getBizNm())) {
                queryString.append("bizNm=").append(URLEncoder.encode(request.getBizNm(), "UTF-8")).append("&");
            }
            if (isNotEmpty(request.getBizNo())) {
                queryString.append("bizNo=").append(URLEncoder.encode(request.getBizNo(), "UTF-8")).append("&");
            }
            if (isNotEmpty(request.getSellerId())) {
                queryString.append("sellerId=").append(URLEncoder.encode(request.getSellerId(), "UTF-8")).append("&");
            }
            if (isNotEmpty(request.getCorpRegNo())) {
                queryString.append("corpRegNo=").append(URLEncoder.encode(request.getCorpRegNo(), "UTF-8")).append("&");
            }
            if (isNotEmpty(request.getCity())) {
                queryString.append("city=").append(URLEncoder.encode(request.getCity(), "UTF-8")).append("&");
            }
            if (isNotEmpty(request.getDistrict())) {
                queryString.append("district=").append(URLEncoder.encode(request.getDistrict(), "UTF-8")).append("&");
            }

            queryString.append("page=").append(request.getPage())
                    .append("&size=").append(request.getSize());

        } catch (UnsupportedEncodingException e) {
            log.error("URL 인코딩 실패", e);
        }

        return queryString.toString();
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}