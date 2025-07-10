package com.antock.web.region.presentation;

import com.antock.api.coseller.application.service.RegionStatusService;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.value.City;
import com.antock.api.coseller.value.District;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/region/status")
public class RegionStatusWebController {
    private final RegionStatusService regionStatusService;

    @GetMapping
    public String showStatus(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            Model model) {

        List<CorpMast> corpList = regionStatusService.getCorpMastList(city, district);

        model.addAttribute("cities", Arrays.asList(City.values()));
        model.addAttribute("districts", Arrays.asList(District.values()));
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedDistrict", district);
        model.addAttribute("corpList", corpList);

        return "region/status";
    }
}