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
public class CorpMastManualService {

    private final CorpMastManualRepository corpMastSearchRepository;
    private final CorpMastHistoryRepository corpMastHistoryRepository;

    public Page<CorpMastManualResponse> search(CorpMastManualRequest request) {
        Pageable pageable = createPageable(request);
        Page<CorpMast> corpPage = corpMastSearchRepository.findBySearchConditions(
                nullIfEmpty(request.getBizNmForSearch()),
                nullIfEmpty(request.getBizNoForSearch()),
                nullIfEmpty(request.getSellerIdForSearch()),
                nullIfEmpty(request.getCorpRegNoForSearch()),
                nullIfEmpty(request.getCityForSearch()),
                nullIfEmpty(request.getDistrictForSearch()),
                pageable);
        return corpPage.map(CorpMastManualResponse::from);
    }

    private String nullIfEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    public CorpMastManualResponse getById(Long id) {
        CorpMast entity = corpMastSearchRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "법인정보를 찾을 수 없습니다. ID: " + id));

        return CorpMastManualResponse.from(entity);
    }

    public CorpMastManualResponse getByBizNo(String bizNo) {
        CorpMast entity = corpMastSearchRepository.findByBizNo(bizNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "해당 사업자번호의 법인정보를 찾을 수 없습니다: " + bizNo));

        return CorpMastManualResponse.from(entity);
    }

    public CorpMastManualResponse getByCorpRegNo(String corpRegNo) {
        CorpMast entity = corpMastSearchRepository.findByCorpRegNo(corpRegNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,
                        "해당 법인등록번호의 법인정보를 찾을 수 없습니다: " + corpRegNo));

        return CorpMastManualResponse.from(entity);
    }

    public List<String> getAllCities() {
        return corpMastSearchRepository.findDistinctCities();
    }

    public List<String> getDistrictsByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return List.of();
        }
        return corpMastSearchRepository.findDistinctDistrictsByCity(city);
    }

    public Map<String, Object> getSearchStatistics(CorpMastManualRequest request) {
        Page<CorpMast> result = corpMastSearchRepository.findBySearchConditions(
                request.getBizNmForSearch(),
                request.getBizNoForSearch(),
                request.getSellerIdForSearch(),
                request.getCorpRegNoForSearch(),
                request.getCityForSearch(),
                request.getDistrictForSearch(),
                PageRequest.of(0, 1));

        long totalCount = result.getTotalElements();

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
        if (request.getSort() != null && !request.getSort().isEmpty()) {
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
        return PageRequest.of(
                Math.max(0, request.getPage()),
                Math.min(100, Math.max(1, request.getSize())),
                sort);
    }

    // 검색 + 페이징
    public Page<CorpMastManualResponse> search(CorpMastForm form, Pageable pageable, String username, boolean isAdmin) {
        Page<CorpMast> page = corpMastSearchRepository.searchCorpMast(
                isAdmin, username,
                form.getBizNm(), form.getBizNo(), form.getCorpRegNo(),
                form.getSiNmForSearch(), form.getSggNmForSearch(), pageable);
        return page.map(CorpMastManualResponse::from);
    }

    // 등록
    @Transactional
    public CorpMast save(CorpMastForm form, String username) {
        CorpMast entity = CorpMast.builder()
                .sellerId(form.getSellerId() != null ? form.getSellerId() : "")
                .bizNm(form.getBizNm() != null ? form.getBizNm() : "")
                .bizNo(form.getBizNo() != null ? form.getBizNo() : "")
                .corpRegNo(form.getCorpRegNo() != null ? form.getCorpRegNo() : "")
                .regionCd(form.getRegionCd() != null ? form.getRegionCd() : "") // NULL 처리
                .siNm(form.getSiNm() != null ? form.getSiNm() : "")
                .sggNm(form.getSggNm() != null ? form.getSggNm() : "")
                .description(form.getDescription() != null ? form.getDescription() : "")
                .username(username)
                .build();
        CorpMast saved = corpMastSearchRepository.save(entity);

        corpMastHistoryRepository.save(CorpMastHistory.builder()
                .username(username)
                .action("CREATE")
                .bizNo(saved.getBizNo())
                .result("SUCCESS")
                .message("수기 등록")
                .timestamp(java.time.LocalDateTime.now())
                .build());
        return saved;
    }

    // 수정
    @Transactional
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

        corpMastHistoryRepository.save(CorpMastHistory.builder()
                .username(username)
                .action("UPDATE")
                .bizNo(entity.getBizNo())
                .result("SUCCESS")
                .message("수기 수정")
                .timestamp(java.time.LocalDateTime.now())
                .build());
        return entity;
    }

    // 삭제
    @Transactional
    public void delete(Long id, String username, boolean isAdmin) {
        CorpMast entity = corpMastSearchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("법인정보 없음"));
        if (!isAdmin && !entity.getUsername().equals(username)) {
            throw new RuntimeException("본인 소유만 삭제 가능");
        }
        corpMastSearchRepository.delete(entity);

        corpMastHistoryRepository.save(CorpMastHistory.builder()
                .username(username)
                .action("DELETE")
                .bizNo(entity.getBizNo())
                .result("SUCCESS")
                .message("수기 삭제")
                .timestamp(java.time.LocalDateTime.now())
                .build());
    }

    // 상세
    public CorpMastManualResponse getById(Long id, String username, boolean isAdmin) {
        CorpMast entity = corpMastSearchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("법인정보 없음"));
        if (!isAdmin && !entity.getUsername().equals(username)) {
            throw new RuntimeException("본인 소유만 조회 가능");
        }
        return CorpMastManualResponse.from(entity);
    }

}