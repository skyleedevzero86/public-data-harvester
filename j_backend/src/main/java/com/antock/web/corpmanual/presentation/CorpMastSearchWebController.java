package com.antock.web.corpmanual.presentation;

import com.antock.api.corpmanual.application.dto.request.CorpMastForm;
import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import com.antock.api.member.application.service.MemberDomainService;
import com.antock.global.utils.NumberFormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    private final MemberDomainService memberDomainService;

    @GetMapping("/list")
    public String list(
            @ModelAttribute CorpMastForm form,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<CorpMastManualResponse> corpList = corpMastService.findForList(form, page, size);

        model.addAttribute("corpList", corpList);
        model.addAttribute("form", form);
        attachListAccess(model, corpList);
        return "corp/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new CorpMastForm());
        return "corp/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute CorpMastForm form) {
        log.warn("법인 생성 기능은 아직 구현되지 않았습니다.");
        return "redirect:/corp/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            CorpMastManualResponse corp = corpMastService.getById(id);
            CorpMastForm form = new CorpMastForm();

            form.setId(corp.getId());
            form.setSellerId(corp.getSellerId());
            form.setBizNm(corp.getBizNm());
            form.setBizNo(corp.getBizNo());
            form.setCorpRegNo(corp.getCorpRegNo());
            form.setRegionCd(corp.getRegionCd());
            form.setSiNm(corp.getSiNm());
            form.setSggNm(corp.getSggNm());
            form.setDescription(corp.getDescription());
            model.addAttribute("form", form);
            return "corp/form";
        } catch (Exception e) {
            log.error("법인 정보 조회 실패: ID = {}", id, e);
            model.addAttribute("errorMessage", "법인 정보를 찾을 수 없습니다.");
            return "redirect:/corp/list";
        }
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute CorpMastForm form) {
        log.warn("법인 수정 기능은 아직 구현되지 않았습니다.");
        return "redirect:/corp/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!canChange(loadOwner(id))) {
                redirectAttributes.addFlashAttribute("message", "등록한 계정 또는 관리자만 삭제할 수 있습니다.");
                return "redirect:/corp/list";
            }
        } catch (Exception e) {
            log.error("법인 삭제 권한 확인 실패: ID = {}", id, e);
            redirectAttributes.addFlashAttribute("message", "법인 정보를 찾을 수 없습니다.");
            return "redirect:/corp/list";
        }
        log.warn("법인 삭제 기능은 아직 구현되지 않았습니다.");
        return "redirect:/corp/list";
    }

    @GetMapping("/modify/{id}")
    public String Modify(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CorpMastManualResponse corp = corpMastService.getById(id);
            if (!canChange(corp.getUsername())) {
                redirectAttributes.addFlashAttribute("message", "등록한 계정 또는 관리자만 수정할 수 있습니다.");
                return "redirect:/corp/list";
            }
            model.addAttribute("corp", corp);
            model.addAttribute("isAdmin", staff(SecurityContextHolder.getContext().getAuthentication()));
            return "corp/list";
        } catch (Exception e) {
            log.error("법인 정보 조회 실패: ID = {}", id, e);
            redirectAttributes.addFlashAttribute("message", "법인 정보를 찾을 수 없습니다.");
            return "redirect:/corp/list";
        }
    }

    @GetMapping("/search")
    public String searchPage(
            @ModelAttribute CorpMastManualRequest searchRequest,
            Model model) {

        log.debug("법인 검색 페이지 요청: {}", searchRequest);

        List<CorpMastSearchResponse> corpList = null;
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
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = staff(auth);
            model.addAttribute("corp", corp);
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("hasEditPermission", canChange(corp.getUsername()));
            if (isAdmin) {
                model.addAttribute("registrantName", registrantName(corp.getUsername()));
            }
            model.addAttribute("shownBizNo", visibleNumber(corp.getFormattedBizNo(), isAdmin));
            model.addAttribute("shownCorpRegNo", visibleNumber(corp.getCorpRegNo(), isAdmin));

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

    private void attachListAccess(Model model, Page<CorpMastManualResponse> corpList) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean staff = staff(auth);
        model.addAttribute("isStaff", staff);
        model.addAttribute("currentUsername", auth == null ? "" : auth.getName());
        if (!staff) {
            return;
        }
        List<String> owners = corpList.getContent().stream()
                .map(CorpMastManualResponse::getUsername)
                .toList();
        model.addAttribute("registrantNames", memberDomainService.nicknamesByUsernames(owners));
    }

    private String registrantName(String username) {
        return memberDomainService.nicknamesByUsernames(List.of(username == null ? "" : username))
                .getOrDefault(username, "");
    }

    private String visibleNumber(String value, boolean staff) {
        if (staff) {
            return value == null ? "" : value;
        }
        return NumberFormatUtil.maskLastDigit(value);
    }

    private String loadOwner(Long id) {
        return corpMastService.getById(id).getUsername();
    }

    private boolean canChange(String owner) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (staff(auth)) {
            return true;
        }
        return auth != null && owner != null && owner.equals(auth.getName());
    }

    private boolean staff(Authentication auth) {
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_MANAGER".equals(authority.getAuthority()));
    }
}