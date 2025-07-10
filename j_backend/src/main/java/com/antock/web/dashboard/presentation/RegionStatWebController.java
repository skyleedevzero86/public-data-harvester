package com.antock.web.dashboard.presentation;

import com.antock.api.dashboard.application.service.RegionStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/region")
public class RegionStatWebController {
    private final RegionStatService regionStatService;

    @GetMapping("/detail")
    public String regionStatusPage(Model model) {
        model.addAttribute("regionStats", regionStatService.getAllRegionStats());
        return "region/DashStatus";
    }
}