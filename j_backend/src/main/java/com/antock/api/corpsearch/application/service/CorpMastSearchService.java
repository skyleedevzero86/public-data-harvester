package com.antock.api.corpsearch.application.service;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpsearch.infrastructure.CorpMastSearchRepository;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorpMastSearchService {

    private final CorpMastSearchRepository corpMastRepository;


    public Page<CorpMastSearchResponse> search(CorpMastSearchRequest request) {
        log.debug("법인 검색 요청: {}", request);

        Pageable pageable = createPageable(request);

        Page<CorpMast> corpPage = corpMastRepository.findBySearchConditions(
                request.getBizNmForSearch(),
                request.getBizNoForSearch(),
                request.getSellerIdForSearch(),
                request.getCorpRegNoForSearch(),
                request.getCityForSearch(),
                request.getDistrictForSearch(),
                pageable
        );

        Page<CorpMastSearchResponse> result = corpPage.map(CorpMastSearchResponse::from);

        log.debug("검색 결과: 총 {}건, 현재 페이지 {}건",
                result.getTotalElements(), result.getNumberOfElements());

        return result;
    }

    public CorpMastSearchResponse getById(Long id) {
        CorpMast entity = corpMastRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "법인정보를 찾을 수 없습니다. ID: " + id));

        return CorpMastSearchResponse.from(entity);
    }

    public CorpMastSearchResponse getByBizNo(String bizNo) {
        CorpMast entity = corpMastRepository.findByBizNo(bizNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "해당 사업자번호의 법인정보를 찾을 수 없습니다: " + bizNo));

        return CorpMastSearchResponse.from(entity);
    }

    public CorpMastSearchResponse getByCorpRegNo(String corpRegNo) {
        CorpMast entity = corpMastRepository.findByCorpRegNo(corpRegNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "해당 법인등록번호의 법인정보를 찾을 수 없습니다: " + corpRegNo));

        return CorpMastSearchResponse.from(entity);
    }

    public List<String> getAllCities() {
        return corpMastRepository.findDistinctCities();
    }

    public List<String> getDistrictsByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return List.of();
        }
        return corpMastRepository.findDistinctDistrictsByCity(city);
    }

    public Map<String, Object> getSearchStatistics(CorpMastSearchRequest request) {
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                request.getBizNmForSearch(),
                request.getBizNoForSearch(),
                request.getSellerIdForSearch(),
                request.getCorpRegNoForSearch(),
                request.getCityForSearch(),
                request.getDistrictForSearch(),
                PageRequest.of(0, 1)
        );

        long totalCount = result.getTotalElements();

        Map<String, Long> locationStats = null;
        if (request.hasSearchCondition()) {
            locationStats = Map.of("검색결과", totalCount);
        }

        return Map.of(
                "totalCount", totalCount,
                "locationStats", locationStats != null ? locationStats : Map.of()
        );
    }

    private Pageable createPageable(CorpMastSearchRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        if (request.getSort() != null && !request.getSort().isEmpty()) {
            String[] sortParts = request.getSort().split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                String direction = sortParts[1];

                Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC : Sort.Direction.DESC;

                sort = Sort.by(sortDirection, property);
            }
        }

        return PageRequest.of(
                Math.max(0, request.getPage()),
                Math.min(100, Math.max(1, request.getSize())),
                sort
        );
    }
}