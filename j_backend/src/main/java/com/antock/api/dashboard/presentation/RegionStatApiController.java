package com.antock.api.dashboard.presentation;

import com.antock.api.dashboard.application.dto.RegionStatDto;
import com.antock.api.dashboard.application.service.RegionStatService;
import com.antock.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/region-stats")
@RequiredArgsConstructor
public class RegionStatApiController {
    private final RegionStatService regionStatService;

    @GetMapping("/top")
    public ApiResponse<RegionStatDto> getTopRegionStat() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getTopRegionStat());
    }

    @GetMapping
    public ApiResponse<List<RegionStatDto>> getAllRegionStats() {
        return ApiResponse.of(HttpStatus.OK, regionStatService.getAllRegionStats());
    }
}