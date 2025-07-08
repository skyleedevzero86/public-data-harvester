package com.antock.api.corpsearch.application.service;

import com.antock.api.corpsearch.application.dto.request.CorpMastSearchRequest;
import com.antock.api.corpsearch.application.dto.response.CorpMastSearchResponse;
import com.antock.api.coseller.domain.CorpMast;
import com.antock.global.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorpExcelService {

    private final CorpMastSearchService corpMastSearchService;
    private final ExcelUtils excelUtils;

    private static final int MAX_EXCEL_RECORDS = 10000;
    private static final int PAGE_SIZE = 1000;

    public byte[] generateExcel(CorpMastSearchRequest searchRequest) throws IOException {

        log.debug("Excel 생성 요청: {}", searchRequest);

        validateSearchRequest(searchRequest);

        List<CorpMastSearchResponse> allData = getAllSearchResults(searchRequest);

        if (allData.size() > MAX_EXCEL_RECORDS) {
            throw new IllegalArgumentException(
                    String.format("Excel 다운로드는 최대 %,d건까지만 가능합니다. (검색결과: %,d건)",
                            MAX_EXCEL_RECORDS, allData.size())
            );
        }

        log.debug("Excel 생성 데이터 수: {} 건", allData.size());

        return excelUtils.createCorpSearchExcel(allData, allData.size());
    }

    private void validateSearchRequest(CorpMastSearchRequest searchRequest) {
        if (!searchRequest.hasSearchCondition()) {
            throw new IllegalArgumentException("Excel 다운로드를 위해서는 최소 하나 이상의 검색 조건이 필요합니다.");
        }
    }

    private List<CorpMastSearchResponse> getAllSearchResults(CorpMastSearchRequest searchRequest) {

        log.debug("Repository를 통한 직접 조회 시작");

        List<CorpMast> entities = corpMastSearchService.getAllEntitiesForExcel(searchRequest);

        log.debug("직접 조회 완료: {} 건", entities.size());

        if (entities.size() > MAX_EXCEL_RECORDS) {
            throw new IllegalArgumentException(
                    String.format("Excel 다운로드는 최대 %,d건까지만 가능합니다. (검색결과: %,d건)",
                            MAX_EXCEL_RECORDS, entities.size())
            );
        }

        return entities.stream()
                .map(CorpMastSearchResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }

    private CorpMastSearchRequest createPageRequest(CorpMastSearchRequest originalRequest, int page) {
        CorpMastSearchRequest pageRequest = new CorpMastSearchRequest();

        pageRequest.setBizNm(originalRequest.getBizNm());
        pageRequest.setBizNo(originalRequest.getBizNo());
        pageRequest.setSellerId(originalRequest.getSellerId());
        pageRequest.setCorpRegNo(originalRequest.getCorpRegNo());
        pageRequest.setCity(originalRequest.getCity());
        pageRequest.setDistrict(originalRequest.getDistrict());

        pageRequest.setPage(page);
        pageRequest.setSize(PAGE_SIZE);
        pageRequest.setSort("id,desc");

        return pageRequest;
    }

    public String generateFileName() {
        return excelUtils.generateFileName();
    }

    public int getMaxExcelRecords() {
        return MAX_EXCEL_RECORDS;
    }
}