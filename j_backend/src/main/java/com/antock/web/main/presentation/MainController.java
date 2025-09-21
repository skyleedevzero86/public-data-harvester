package com.antock.web.main.presentation;

import com.antock.api.dashboard.application.dto.RecentActivityDto;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.dashboard.application.service.DashboardService;
import com.antock.api.dashboard.application.service.RegionStatService;
import com.antock.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class MainController {

    private final DashboardService dashboardService;
    private final RegionStatService regionStatService;

    @GetMapping
    public String index(Model model) {
        DashboardService.DashboardStats stats = dashboardService.getStats();
        List<RecentActivityDto> recentActivities = dashboardService.getRecentActivities(8);
        RegionStatDto topRegionStat = regionStatService.getTopRegionStat();

        if (topRegionStat == null) {
            topRegionStat = new RegionStatDto();
        }

        List<RegionStatDto> regionStats = regionStatService.getAllRegionStats();
        if (regionStats.size() > 5) {
            regionStats = regionStats.subList(0, 5);
        }

        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("stats", stats);
        model.addAttribute("topRegionStat", topRegionStat);
        model.addAttribute("regionStats", regionStats);
        return "main/index";
    }

    @GetMapping("/api/stats")
    public ApiResponse<DashboardService.DashboardStats> getStats() {
        DashboardService.DashboardStats stats = dashboardService.getStats();
        return ApiResponse.success(stats, "통계 정보를 성공적으로 조회했습니다.");
    }
}
