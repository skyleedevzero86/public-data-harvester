package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.infrastructure.CorpMastRepository;
import com.antock.api.dashboard.application.dto.RegionStatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionStatService {
    private final CorpMastRepository corpMastRepository;

    public RegionStatDto getTopRegionStat() {
        List<RegionStatDto> allStats = getAllRegionStats();
        return allStats.isEmpty() ? null : allStats.get(0);
    }

    public List<RegionStatDto> getAllRegionStats() {
        List<Object[]> rawStats = corpMastRepository.getRegionStats();

        return rawStats.stream()
                .map(this::convertToRegionStatDto)
                .collect(Collectors.toList());
    }

    private RegionStatDto convertToRegionStatDto(Object[] rawData) {
        String city = (String) rawData[0];
        String district = (String) rawData[1];
        Long totalCount = ((Number) rawData[2]).longValue();
        Long validCorpRegNoCount = ((Number) rawData[3]).longValue();
        Long validRegionCdCount = ((Number) rawData[4]).longValue();

        return new RegionStatDto(city, district, totalCount, validCorpRegNoCount, validRegionCdCount);
    }
}