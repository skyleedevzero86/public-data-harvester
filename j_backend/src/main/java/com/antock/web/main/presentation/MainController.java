package com.antock.web.main.presentation;

import com.antock.api.dashboard.application.dto.RecentActivityDto;
import com.antock.api.dashboard.application.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class MainController{

    private final DashboardService dashboardService;

    @GetMapping
    public String index(Model model) {
        DashboardService.DashboardStats stats = dashboardService.getStats();
        List<RecentActivityDto> recentActivities = dashboardService.getRecentActivities(5);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("stats", stats);
         return "main/index";
    }
}
