package com.antock.web.region.presentation;

import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.dashboard.application.service.RegionStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/region")
public class RegionStatusWebController {

    private final RegionStatService regionStatService;

    @GetMapping("/status")
    public String regionStatusPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            Model model) {

        try {
            log.info("Region status page request - page: {}, size: {}, city: {}, district: {}",
                    page, size, city, district);

            Pageable pageable = PageRequest.of(page, size);
            Page<RegionStatDto> regionStatsPage = regionStatService.getRegionStatsWithPaging(pageable, city, district);

            List<String> cities = regionStatService.getCities();
            List<String> districts = city != null ? regionStatService.getDistrictsByCity(city) : List.of();

            model.addAttribute("regionStats", regionStatsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", regionStatsPage.getTotalPages());
            model.addAttribute("totalElements", regionStatsPage.getTotalElements());
            model.addAttribute("hasNext", regionStatsPage.hasNext());
            model.addAttribute("hasPrevious", regionStatsPage.hasPrevious());
            model.addAttribute("isFirst", regionStatsPage.isFirst());
            model.addAttribute("isLast", regionStatsPage.isLast());
            model.addAttribute("size", size);
            model.addAttribute("city", city);
            model.addAttribute("district", district);
            model.addAttribute("cities", cities);
            model.addAttribute("districts", districts);

            log.info("Region status page loaded successfully - total elements: {}, current page: {}",
                    regionStatsPage.getTotalElements(), page);

            return "region/status";
        } catch (Exception e) {
            log.error("Error loading region status page", e);
            model.addAttribute("error", "데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("regionStats", List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0);
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrevious", false);
            model.addAttribute("isFirst", true);
            model.addAttribute("isLast", true);
            model.addAttribute("size", size);
            model.addAttribute("city", city);
            model.addAttribute("district", district);
            model.addAttribute("cities", List.of());
            model.addAttribute("districts", List.of());
            return "region/status";
        }
    }

}