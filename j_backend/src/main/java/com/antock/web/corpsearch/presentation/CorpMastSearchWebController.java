package com.antock.web.corpsearch.presentation;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpsearch.application.service.CorpMastSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/corp")
@RequiredArgsConstructor
public class CorpMastSearchWebController {

    private final CorpMastSearchService corpMastService;

    @GetMapping("/search")
    public String searchPage(
            @ModelAttribute CorpMastSearchRequest searchRequest,
            Model model) {

        log.debug("법인 검색 페이지 요청: {}", searchRequest);

        Page<CorpMastSearchResponse> corpList = null;
        Map<String, Object> statistics = null;

        if (searchRequest.hasSearchCondition()) {
            corpList = corpMastService.search(searchRequest);
            statistics = corpMastService.getSearchStatistics(searchRequest);
        }

        List<String> cities = corpMastService.getAllCities();

        List<String> districts = null;
        if (searchRequest.getCity() != null && !searchRequest.getCity().isEmpty()) {
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
    public String detailPage(@PathVariable Long id, Model model) {

        log.debug("법인 상세 페이지 요청: ID = {}", id);

        try {
            CorpMastSearchResponse corp = corpMastService.getById(id);
            model.addAttribute("corp", corp);

            return "corp/detail";

        } catch (Exception e) {
            log.error("법인 상세 조회 실패: ID = {}", id, e);
            model.addAttribute("errorMessage", "법인 정보를 찾을 수 없습니다.");
            return "redirect:/corp/search";
        }
    }

    @GetMapping("/districts/{city}")
    @ResponseBody
    public List<String> getDistrictsByCity(@PathVariable String city) {
        log.debug("구/군 목록 조회 요청: city = {}", city);
        return corpMastService.getDistrictsByCity(city);
    }

    @PostMapping("/search/reset")
    public String resetSearch(RedirectAttributes redirectAttributes) {
        log.debug("검색 조건 초기화 요청");
        redirectAttributes.addFlashAttribute("message", "검색 조건이 초기화되었습니다.");
        return "redirect:/corp/search";
    }

    @GetMapping("/export")
    public String exportToExcel(
            @ModelAttribute CorpMastSearchRequest searchRequest,
            RedirectAttributes redirectAttributes) {

        log.debug("Excel 다운로드 요청: {}", searchRequest);
        redirectAttributes.addFlashAttribute("message", "Excel 다운로드 기능은 준비 중입니다.");
        return "redirect:/corp/search?" + buildQueryString(searchRequest);
    }

    private String buildQueryString(CorpMastSearchRequest request) {
        StringBuilder queryString = new StringBuilder();

        if (request.getBizNm() != null && !request.getBizNm().isEmpty()) {
            queryString.append("bizNm=").append(request.getBizNm()).append("&");
        }
        if (request.getBizNo() != null && !request.getBizNo().isEmpty()) {
            queryString.append("bizNo=").append(request.getBizNo()).append("&");
        }
        if (request.getSellerId() != null && !request.getSellerId().isEmpty()) {
            queryString.append("sellerId=").append(request.getSellerId()).append("&");
        }
        if (request.getCorpRegNo() != null && !request.getCorpRegNo().isEmpty()) {
            queryString.append("corpRegNo=").append(request.getCorpRegNo()).append("&");
        }
        if (request.getCity() != null && !request.getCity().isEmpty()) {
            queryString.append("city=").append(request.getCity()).append("&");
        }
        if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
            queryString.append("district=").append(request.getDistrict()).append("&");
        }

        queryString.append("page=").append(request.getPage())
                .append("&size=").append(request.getSize());

        return queryString.toString();
    }
}