package com.antock.api.coseller.application.service;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionStatusService {

    private final CorpMastRepository corpMastRepository;

    public List<CorpMast> getCorpMastList(String city, String district) {
        if (StringUtils.hasText(city)) {
            if (StringUtils.hasText(district)) {
                return corpMastRepository.findBySiNmAndSggNm(city, district);
            } else {
                return corpMastRepository.findBySiNm(city);
            }
        }
        return corpMastRepository.findAll();
    }

    public List<String> getDistrictsByCity(String city) {
        if (!StringUtils.hasText(city)) {
            return List.of();
        }

        return com.antock.api.coseller.value.District.getDistrictsByCity(city)
                .stream()
                .map(com.antock.api.coseller.value.District::getValue)
                .collect(java.util.stream.Collectors.toList());
    }
}