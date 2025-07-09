package com.antock.web.corpmanual.presentation;

import com.antock.api.corpmanual.application.dto.request.CorpMastForm;
import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final CorpMastManualService corpMastService;

    @GetMapping("/list")
    public String list(
            @ModelAttribute CorpMastForm form,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // 검색 조건이 있는 경우에만 검색 실행
        Page<CorpMastManualResponse> corpList = null;
        if (hasSearchCondition(form)) {
            corpList = corpMastService.search(form, pageable, username, isAdmin);
        } else {
            // 검색 조건이 없으면 빈 페이지 생성
            corpList = Page.empty(pageable);
        }

        model.addAttribute("corpList", corpList);
        model.addAttribute("form", form);
        model.addAttribute("isAdmin", isAdmin);
        return "corp/list";
    }

    private boolean hasSearchCondition(CorpMastForm form) {
        return (form.getBizNm() != null && !form.getBizNm().trim().isEmpty()) ||
                (form.getBizNo() != null && !form.getBizNo().trim().isEmpty()) ||
                (form.getCorpRegNo() != null && !form.getCorpRegNo().trim().isEmpty()) ||
                (form.getSiNm() != null && !form.getSiNm().trim().isEmpty()) ||
                (form.getSggNm() != null && !form.getSggNm().trim().isEmpty());
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new CorpMastForm());
        return "corp/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute CorpMastForm form) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        corpMastService.save(form, username);
        return "redirect:/corp/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        CorpMastManualResponse corp = corpMastService.getById(id, username, isAdmin);
        CorpMastForm form = new CorpMastForm();

        form.setId(corp.getId());
        form.setSellerId(corp.getSellerId());
        form.setBizNm(corp.getBizNm());
        form.setBizNo(corp.getBizNo());
        form.setCorpRegNo(corp.getCorpRegNo());
        form.setRegionCd(corp.getRegionCd());
        form.setSiNm(corp.getSiNm());
        form.setSggNm(corp.getSggNm());
        model.addAttribute("form", form);
        return "corp/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute CorpMastForm form) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        corpMastService.update(id, form, username, isAdmin);
        return "redirect:/corp/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        corpMastService.delete(id, username, isAdmin);
        return "redirect:/corp/list";
    }

    @GetMapping("/modify/{id}")
    public String Modify(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        CorpMastManualResponse corp = corpMastService.getById(id, username, isAdmin);
        model.addAttribute("corp", corp);
        model.addAttribute("isAdmin", isAdmin);
        return "corp/modify";
    }


    @GetMapping("/search")
    public String searchPage(
            @ModelAttribute CorpMastManualRequest searchRequest,
            Model model) {

        log.debug("법인 검색 페이지 요청: {}", searchRequest);

        Page<CorpMastManualResponse> corpList = null;
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
            CorpMastManualResponse corp = corpMastService.getById(id);
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
            @ModelAttribute CorpMastManualRequest searchRequest,
            RedirectAttributes redirectAttributes) {

        log.debug("Excel 다운로드 요청: {}", searchRequest);
        redirectAttributes.addFlashAttribute("message", "Excel 다운로드 기능은 준비 중입니다.");
        return "redirect:/corp/search?" + buildQueryString(searchRequest);
    }

    private String buildQueryString(CorpMastManualRequest request) {
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