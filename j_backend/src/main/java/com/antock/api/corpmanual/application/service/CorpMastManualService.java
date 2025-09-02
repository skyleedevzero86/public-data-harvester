package com.antock.api.corpmanual.application.service;

import com.antock.api.corpmanual.application.dto.request.CorpMastForm;
import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpmanual.infrastructure.CorpMastManualRepository;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorpMastManualService {

    private final CorpMastManualRepository corpMastManualRepository;
    private final Executor asyncExecutor;

    @Cacheable(value = "corpMastSearch", key = "#request.toString()")
    public List<CorpMastSearchResponse> search(CorpMastManualRequest request) {
        log.info("법인 정보 검색 요청: {}", request);

        Page<CorpMast> results = corpMastManualRepository.findBySearchConditions(
                request.getBizNm(),
                request.getBizNo(),
                request.getSellerId(),
                request.getCorpRegNo(),
                request.getCity(),
                request.getDistrict(),
                PageRequest.of(0, Integer.MAX_VALUE)
        );

        return results.getContent().stream()
                .map(this::convertToSearchResponse)
                .toList();
    }

    @Cacheable(value = "corpMastSearchPagination", key = "#request.toString() + '_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDir")
    public Page<CorpMastSearchResponse> searchWithPagination(
            CorpMastManualRequest request,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.info("법인 정보 페이징 검색 요청: page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CorpMast> corpMastPage = corpMastManualRepository.findBySearchConditions(
                request.getBizNm(),
                request.getBizNo(),
                request.getSellerId(),
                request.getCorpRegNo(),
                request.getCity(),
                request.getDistrict(),
                pageable
        );

        return corpMastPage.map(this::convertToSearchResponse);
    }

    @Cacheable(value = "corpMastById", key = "#id")
    public CorpMastManualResponse getById(Long id) {
        log.info("법인 정보 조회 요청 - ID: {}", id);

        CorpMast corpMast = corpMastManualRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CORP_NOT_FOUND));

        return convertToManualResponse(corpMast);
    }

    @Cacheable(value = "corpMastByBizNo", key = "#bizNo")
    public CorpMastManualResponse getByBizNo(String bizNo) {
        log.info("사업자번호로 법인 정보 조회 요청: {}", bizNo);

        String normalizedBizNo = normalizeBizNo(bizNo);

        CorpMast corpMast = corpMastManualRepository.findByBizNo(normalizedBizNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CORP_NOT_FOUND));

        return convertToManualResponse(corpMast);
    }

    @Cacheable(value = "corpMastByCorpRegNo", key = "#corpRegNo")
    public CorpMastManualResponse getByCorpRegNo(String corpRegNo) {
        log.info("법인등록번호로 법인 정보 조회 요청: {}", corpRegNo);

        String normalizedCorpRegNo = normalizeCorpRegNo(corpRegNo);

        CorpMast corpMast = corpMastManualRepository.findByCorpRegNo(normalizedCorpRegNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.CORP_NOT_FOUND));

        return convertToManualResponse(corpMast);
    }

    @Cacheable(value = "corpMastCities", key = "'all'")
    public List<String> getAllCities() {
        log.info("도시 목록 조회 요청");

        return corpMastManualRepository.findDistinctCities();
    }

    @Cacheable(value = "corpMastDistricts", key = "#city")
    public List<String> getDistrictsByCity(String city) {
        log.info("구/군 목록 조회 요청 - 도시: {}", city);

        return corpMastManualRepository.findDistinctDistrictsByCity(city);
    }

    @Cacheable(value = "corpMastStatistics", key = "#request.toString()")
    public Map<String, Object> getSearchStatistics(CorpMastManualRequest request) {
        log.info("검색 통계 요청: {}", request);

        long totalCount = corpMastManualRepository.countBySearchConditions(
                request.getBizNm(),
                request.getBizNo(),
                request.getSellerId(),
                request.getCorpRegNo(),
                request.getCity(),
                request.getDistrict()
        );

        Map<String, Object> statistics = Map.of(
                "totalCount", totalCount,
                "searchConditions", request,
                "timestamp", LocalDateTime.now().toString()
        );

        return statistics;
    }

    private CorpMastSearchResponse convertToSearchResponse(CorpMast corpMast) {
        CorpMastManualResponse manualResponse = convertToManualResponse(corpMast);

        return CorpMastSearchResponse.builder()
                .content(List.of(manualResponse))
                .pagination(CorpMastSearchResponse.PaginationInfo.builder()
                        .pageNumber(0)
                        .pageSize(1)
                        .totalElements(1)
                        .totalPages(1)
                        .hasNext(false)
                        .hasPrevious(false)
                        .isFirst(true)
                        .isLast(true)
                        .numberOfElements(1)
                        .build())
                .searchInfo(CorpMastSearchResponse.SearchInfo.builder()
                        .searchConditions(null)
                        .resultCount(1)
                        .searchTime(LocalDateTime.now().toString())
                        .searchSummary("단일 결과")
                        .build())
                .build();
    }

    private CorpMastManualResponse convertToManualResponse(CorpMast corpMast) {
        return CorpMastManualResponse.builder()
                .id(corpMast.getId())
                .sellerId(corpMast.getSellerId())
                .bizNm(corpMast.getBizNm())
                .bizNo(corpMast.getBizNo())
                .corpRegNo(corpMast.getCorpRegNo())
                .regionCd(corpMast.getRegionCd())
                .siNm(corpMast.getSiNm())
                .sggNm(corpMast.getSggNm())
                .username(corpMast.getUsername())
                .description(corpMast.getDescription())
                .build();
    }

    private String normalizeBizNo(String bizNo) {
        if (bizNo == null)
            return null;
        return bizNo.replaceAll("-", "");
    }

    private String normalizeCorpRegNo(String corpRegNo) {
        if (corpRegNo == null)
            return null;
        return corpRegNo.replaceAll("-", "");
    }
}