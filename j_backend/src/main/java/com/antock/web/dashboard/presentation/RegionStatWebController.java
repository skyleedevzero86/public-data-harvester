package com.antock.web.dashboard.presentation;

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
public class RegionStatWebController {
        private final RegionStatService regionStatService;

        @GetMapping("/detail")
        public String regionStatusPage(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size,
                @RequestParam(required = false) String city,
                @RequestParam(required = false) String district,
                Model model) {

                if (city != null && city.trim().isEmpty()) {
                        city = null;
                }
                if (district != null && district.trim().isEmpty()) {
                        district = null;
                }

                log.info("Region detail dashboard request - page: {}, size: {}, city: {}, district: {}",
                        page, size, city, district);

                Pageable pageable = PageRequest.of(page, size);
                Page<RegionStatDto> regionStatsPage = regionStatService.getRegionStatsWithPaging(pageable, city,
                        district);

                List<String> cities = regionStatService.getCities();
                List<String> districts = city != null ? regionStatService.getDistrictsByCity(city) : List.of();

                log.info("Retrieved region stats for dashboard - count: {}, totalPages: {}",
                        regionStatsPage.getContent().size(), regionStatsPage.getTotalPages());

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

                return "region/DashStatus";
        }
}