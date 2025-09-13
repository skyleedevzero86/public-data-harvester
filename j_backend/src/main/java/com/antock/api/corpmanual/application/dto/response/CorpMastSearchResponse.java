package com.antock.api.corpmanual.application.dto.response;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@Schema(description = "법인 정보 검색 응답 DTO", example = """
        {
          "content": [...],
          "pagination": {
            "pageNumber": 0,
            "pageSize": 20,
            "totalElements": 1500,
            "totalPages": 75,
            "hasNext": true,
            "hasPrevious": false,
            "nextPage": 1,
            "previousPage": 0
          },
          "searchInfo": {
            "searchConditions": {...},
            "resultCount": 20,
            "searchTime": "2024-01-15T10:30:00"
          }
        }
        """)
public class CorpMastSearchResponse {

    @Schema(description = "검색 결과 법인 목록", required = true)
    private List<CorpMastManualResponse> content;

    @Schema(description = "페이징 정보", required = true)
    private PaginationInfo pagination;

    @Schema(description = "검색 정보", required = true)
    private SearchInfo searchInfo;

    @Getter
    @Builder
    @Schema(description = "페이징 정보")
    public static class PaginationInfo {
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int pageNumber;

        @Schema(description = "페이지 크기", example = "20")
        private int pageSize;

        @Schema(description = "전체 요소 수", example = "1500")
        private long totalElements;

        @Schema(description = "전체 페이지 수", example = "75")
        private int totalPages;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        private boolean hasPrevious;

        @Schema(description = "다음 페이지 번호", example = "1")
        private Integer nextPage;

        @Schema(description = "이전 페이지 번호", example = "0")
        private Integer previousPage;

        @Schema(description = "첫 페이지 여부", example = "true")
        private boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private boolean isLast;

        @Schema(description = "현재 페이지의 요소 수", example = "20")
        private int numberOfElements;

    }

    @Getter
    @Builder
    @Schema(description = "검색 정보")
    public static class SearchInfo {
        @Schema(description = "검색 조건", required = true)
        private CorpMastManualRequest searchConditions;

        @Schema(description = "검색 결과 수", example = "20")
        private int resultCount;

        @Schema(description = "검색 실행 시간", example = "2024-01-15T10:30:00")
        private String searchTime;

        @Schema(description = "검색 조건 요약", example = "법인명: 삼성, 지역: 서울특별시")
        private String searchSummary;
    }

    public static CorpMastSearchResponse from(Page<CorpMastManualResponse> page, CorpMastManualRequest request) {
        PaginationInfo pagination = PaginationInfo.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .nextPage(page.hasNext() ? page.getNumber() + 1 : null)
                .previousPage(page.hasPrevious() ? page.getNumber() - 1 : null)
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .build();

        SearchInfo searchInfo = SearchInfo.builder()
                .searchConditions(request)
                .resultCount(page.getNumberOfElements())
                .searchTime(java.time.LocalDateTime.now().toString())
                .searchSummary(generateSearchSummary(request))
                .build();

        return CorpMastSearchResponse.builder()
                .content(page.getContent())
                .pagination(pagination)
                .searchInfo(searchInfo)
                .build();
    }

    private static String generateSearchSummary(CorpMastManualRequest request) {
        StringBuilder summary = new StringBuilder();

        if (request.getBizNm() != null && !request.getBizNm().trim().isEmpty()) {
            summary.append("법인명: ").append(request.getBizNm().trim());
        }

        if (request.getBizNo() != null && !request.getBizNo().trim().isEmpty()) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("사업자번호: ").append(request.getBizNo().trim());
        }

        if (request.getSellerId() != null && !request.getSellerId().trim().isEmpty()) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("판매자ID: ").append(request.getSellerId().trim());
        }

        if (request.getCorpRegNo() != null && !request.getCorpRegNo().trim().isEmpty()) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("법인등록번호: ").append(request.getCorpRegNo().trim());
        }

        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            if (summary.length() > 0)
                summary.append(", ");
            summary.append("지역: ").append(request.getCity().trim());

            if (request.getDistrict() != null && !request.getDistrict().trim().isEmpty()) {
                summary.append(" ").append(request.getDistrict().trim());
            }
        }

        return summary.length() > 0 ? summary.toString() : "전체 검색";
    }
}