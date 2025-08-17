package com.antock.api.corpmanual.application.service;

import com.antock.api.corpmanual.application.dto.request.CorpMastForm;
import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.infrastructure.CorpMastManualRepository;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.api.coseller.infrastructure.CorpMastHistoryRepository;
import com.antock.global.common.exception.BusinessException;
import com.antock.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorpMastManualService {

    private final CorpMastManualRepository corpMastSearchRepository;
    private final CorpMastHistoryRepository corpMastHistoryRepository;

    @Qualifier("applicationTaskExecutor")
    private final Executor asyncExecutor;

    @Cacheable(value = "corpMast", key = "#request.hashCode()")
    public Page<CorpMastManualResponse> search(CorpMastManualRequest request) {
        Pageable pageable = createPageable(request);

        String bizNm = nullIfEmpty(request.getBizNmForSearch());
        String bizNo = nullIfEmpty(request.getBizNoForSearch());
        String sellerId = nullIfEmpty(request.getSellerIdForSearch());
        String corpRegNo = nullIfEmpty(request.getCorpRegNoForSearch());
        String city = nullIfEmpty(request.getCityForSearch());
        String district = nullIfEmpty(request.getDistrictForSearch());

        Page<CorpMast> corpPage = corpMastSearchRepository.findBySearchConditions(
                bizNm, bizNo, sellerId, corpRegNo, city, district, pageable);

        return corpPage.map(CorpMastManualResponse::from);
    }

    private String nullIfEmpty(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    @Cacheable(value = "corpMast", key = "#id")
    public CorpMastManualResponse getById(Long id) {
        CorpMast entity = corpMastSearchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "법인정보를 찾을 수 없습니다. ID: " + id));

        return CorpMastManualResponse.from(entity);
    }

    @Cacheable(value = "corpMast", key = "'bizNo:' + #bizNo")
    public CorpMastManualResponse getByBizNo(String bizNo) {
        CorpMast entity = corpMastSearchRepository.findByBizNo(bizNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "해당 사업자번호의 법인정보를 찾을 수 없습니다: " + bizNo));

        return CorpMastManualResponse.from(entity);
    }

    @Cacheable(value = "corpMast", key = "'corpRegNo:' + #corpRegNo")
    public CorpMastManualResponse getByCorpRegNo(String corpRegNo) {
        CorpMast entity = corpMastSearchRepository.findByCorpRegNo(corpRegNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "해당 법인등록번호의 법인정보를 찾을 수 없습니다: " + corpRegNo));

        return CorpMastManualResponse.from(entity);
    }

    @Cacheable(value = "cities", key = "'all'")
    public List<String> getAllCities() {
        return corpMastSearchRepository.findDistinctCities();
    }

    @Cacheable(value = "districts", key = "#city")
    public List<String> getDistrictsByCity(String city) {
        if (!StringUtils.hasText(city)) {
            return List.of();
        }
        return corpMastSearchRepository.findDistinctDistrictsByCity(city);
    }

    @Cacheable(value = "searchStats", key = "#request.hashCode()")
    public Map<String, Object> getSearchStatistics(CorpMastManualRequest request) {
        long totalCount = corpMastSearchRepository.countBySearchConditions(
                request.getBizNmForSearch(),
                request.getBizNoForSearch(),
                request.getSellerIdForSearch(),
                request.getCorpRegNoForSearch(),
                request.getCityForSearch(),
                request.getDistrictForSearch());

        Map<String, Long> locationStats = null;
        if (request.hasSearchCondition()) {
            locationStats = Map.of("검색결과", totalCount);
        }

        return Map.of(
                "totalCount", totalCount,
                "locationStats", locationStats != null ? locationStats : Map.of());
    }

    private Pageable createPageable(CorpMastManualRequest request) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        if (StringUtils.hasText(request.getSort())) {
            String[] sortParts = request.getSort().split(",");
            if (sortParts.length == 2) {
                String property = sortParts[0];
                String direction = sortParts[1];
                Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
                sort = Sort.by(sortDirection, property);
            }
        }

        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));

        return PageRequest.of(page, size, sort);
    }

    @Cacheable(value = "corpMastSearch", key = "#form.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #username + '_' + #isAdmin")
    public Page<CorpMastManualResponse> search(CorpMastForm form, Pageable pageable, String username, boolean isAdmin) {
        Page<CorpMast> page = corpMastSearchRepository.searchCorpMast(
                isAdmin, username,
                form.getBizNm(), form.getBizNo(), form.getCorpRegNo(),
                form.getSiNmForSearch(), form.getSggNmForSearch(), pageable);
        return page.map(CorpMastManualResponse::from);
    }

    @Transactional
    @CacheEvict(value = {"corpMast", "corpMastSearch", "searchStats"}, allEntries = true)
    public CorpMast save(CorpMastForm form, String username) {
        CorpMast entity = CorpMast.builder()
                .sellerId(form.getSellerId() != null ? form.getSellerId() : "")
                .bizNm(form.getBizNm() != null ? form.getBizNm() : "")
                .bizNo(form.getBizNo() != null ? form.getBizNo() : "")
                .corpRegNo(form.getCorpRegNo() != null ? form.getCorpRegNo() : "")
                .regionCd(form.getRegionCd() != null ? form.getRegionCd() : "")
                .siNm(form.getSiNm() != null ? form.getSiNm() : "")
                .sggNm(form.getSggNm() != null ? form.getSggNm() : "")
                .description(form.getDescription() != null ? form.getDescription() : "")
                .username(username)
                .build();

        CorpMast saved = corpMastSearchRepository.save(entity);

        CompletableFuture.runAsync(() -> {
            try {
                CorpMastHistory history = CorpMastHistory.builder()
                        .username(username)
                        .action("CREATE")
                        .bizNo(saved.getBizNo())
                        .result("SUCCESS")
                        .message("수기 등록")
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                corpMastHistoryRepository.save(history);
            } catch (Exception e) {
                log.error("히스토리 저장 실패: {}", e.getMessage(), e);
            }
        }, asyncExecutor);

        return saved;
    }

    @Transactional
    @CacheEvict(value = {"corpMast", "corpMastSearch", "searchStats"}, allEntries = true)
    public CorpMast update(Long id, CorpMastForm form, String username, boolean isAdmin) {
        CorpMast entity = corpMastSearchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("법인정보 없음"));

        if (!isAdmin && !entity.getUsername().equals(username)) {
            throw new RuntimeException("본인 소유만 수정 가능");
        }

        entity.setSellerId(form.getSellerId());
        entity.setBizNm(form.getBizNm());
        entity.setBizNo(form.getBizNo());
        entity.setCorpRegNo(form.getCorpRegNo());
        entity.setRegionCd(form.getRegionCd());
        entity.setSiNm(form.getSiNm());
        entity.setSggNm(form.getSggNm());

        corpMastSearchRepository.save(entity);

        CompletableFuture.runAsync(() -> {
            try {
                CorpMastHistory history = CorpMastHistory.builder()
                        .username(username)
                        .action("UPDATE")
                        .bizNo(entity.getBizNo())
                        .result("SUCCESS")
                        .message("수기 수정")
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                corpMastHistoryRepository.save(history);
            } catch (Exception e) {
                log.error("히스토리 저장 실패: {}", e.getMessage(), e);
            }
        }, asyncExecutor);

        return entity;
    }

    @Transactional
    @CacheEvict(value = {"corpMast", "corpMastSearch", "searchStats"}, allEntries = true)
    public void delete(Long id, String username, boolean isAdmin) {
        CorpMast entity = corpMastSearchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("법인정보 없음"));

        if (!isAdmin && !entity.getUsername().equals(username)) {
            throw new RuntimeException("본인 소유만 삭제 가능");
        }

        corpMastSearchRepository.delete(entity);

        CompletableFuture.runAsync(() -> {
            try {
                CorpMastHistory history = CorpMastHistory.builder()
                        .username(username)
                        .action("DELETE")
                        .bizNo(entity.getBizNo())
                        .result("SUCCESS")
                        .message("수기 삭제")
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
                corpMastHistoryRepository.save(history);
            } catch (Exception e) {
                log.error("히스토리 저장 실패: {}", e.getMessage(), e);
            }
        }, asyncExecutor);
    }

    @Cacheable(value = "corpMast", key = "#id + '_' + #username + '_' + #isAdmin")
    public CorpMastManualResponse getById(Long id, String username, boolean isAdmin) {
        CorpMast entity = corpMastSearchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("법인정보 없음"));

        if (!isAdmin && !entity.getUsername().equals(username)) {
            throw new RuntimeException("본인 소유만 조회 가능");
        }

        return CorpMastManualResponse.from(entity);
    }

    @Transactional
    public void saveAll(List<CorpMast> entities) {
        int batchSize = 100;
        for (int i = 0; i < entities.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entities.size());
            List<CorpMast> batch = entities.subList(i, end);
            corpMastSearchRepository.saveAll(batch);
        }
    }

    @Cacheable(value = "regionStats", key = "'all'")
    public List<com.antock.api.dashboard.application.dto.RegionStatDto> getRegionStats() {
        List<Object[]> rawStats = corpMastSearchRepository.getRegionStats();

        return rawStats.stream()
                .map(this::convertToRegionStatDto)
                .collect(Collectors.toList());
    }

    private com.antock.api.dashboard.application.dto.RegionStatDto convertToRegionStatDto(Object[] rawData) {
        String city = (String) rawData[0];
        String district = (String) rawData[1];
        Long totalCount = ((Number) rawData[2]).longValue();
        Long validCorpRegNoCount = ((Number) rawData[3]).longValue();
        Long validRegionCdCount = ((Number) rawData[4]).longValue();

        return new com.antock.api.dashboard.application.dto.RegionStatDto(
                city, district, totalCount, validCorpRegNoCount, validRegionCdCount);
    }

}