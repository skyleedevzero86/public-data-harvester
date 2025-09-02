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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/region")
@RequiredArgsConstructor
public class RegionStatusWebController {

    private final RegionStatService regionStatService;

    @GetMapping("/status")
    public String regionStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            Model model) {

        Sort sort = Sort.by(Sort.Direction.DESC, "totalCount");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RegionStatDto> regionStats = regionStatService.getRegionStatsWithPaging(
                pageable, city, district);

        List<City> cities = Arrays.asList(City.values());

        List<District> districts;
        if (city != null && !city.isEmpty()) {
            districts = District.getDistrictsByCity(city);
        } else {
            districts = Arrays.asList(District.values());
        }

        model.addAttribute("regionStats", regionStats.getContent());
        model.addAttribute("cities", cities);
        model.addAttribute("districts", districts);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedDistrict", district);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", regionStats.getTotalPages());
        model.addAttribute("totalElements", regionStats.getTotalElements());
        model.addAttribute("hasNext", regionStats.hasNext());
        model.addAttribute("hasPrevious", regionStats.hasPrevious());

        return "region/status";
    }
}