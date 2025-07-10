package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionStatService {
    private final CorpMastRepository corpMastRepository;

    public RegionStatDto getTopRegionStat() {
        List<RegionStatDto> stats = corpMastRepository.getRegionStats();
        return stats.isEmpty() ? null : stats.get(0);
    }

    public List<RegionStatDto> getAllRegionStats() {
        List<RegionStatDto> stats = corpMastRepository.getRegionStats();
        System.out.println("regionStats size: " + stats.size());
        return stats;
    }
}