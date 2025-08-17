package com.antock.api.coseller.application.service;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionStatusService {
    private final CorpMastRepository corpMastRepository;

    public List<CorpMast> getCorpMastList(String city, String district) {
        if (city != null && !city.isEmpty() && district != null && !district.isEmpty()) {
            return corpMastRepository.findBySiNmAndSggNm(city, district);
        } else if (city != null && !city.isEmpty()) {
            return corpMastRepository.findBySiNm(city);
        } else {
            return List.of();
        }
    }
}