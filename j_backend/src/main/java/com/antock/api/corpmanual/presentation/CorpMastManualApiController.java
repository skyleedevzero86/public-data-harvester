package com.antock.api.corpmanual.presentation;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.corpmanual.application.dto.response.CorpMastSearchResponse;
import com.antock.api.corpmanual.application.service.CorpMastManualService;
import com.antock.api.corpmanual.application.service.CorpMastManualExcelService;
import com.antock.global.common.response.ApiResponse;
import com.antock.global.security.annotation.CurrentUser;
import com.antock.global.security.dto.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/corp")
@RequiredArgsConstructor
@Tag(name = "CorpMast Management", description = "법인 정보 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class CorpMastManualApiController {

  private static final Logger log = LoggerFactory.getLogger(CorpMastManualApiController.class);
  private final CorpMastManualService corpMastManualService;
  private final CorpMastManualExcelService excelService;

  @Operation(summary = "법인 정보 Excel 내보내기", description = """
      법인 정보를 Excel 파일 형태로 내보냅니다.

      ### 기능
      - 검색 조건에 따른 법인 정보 필터링
      - Excel 파일 다운로드 (.xlsx 형식)
      - 모든 필드 포함하여 내보내기

      ### 주의사항
      - 대용량 데이터의 경우 처리 시간이 오래 걸릴 수 있습니다
      - 로그인된 사용자만 사용 가능합니다
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Excel 파일 다운로드 성공", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", schema = @Schema(type = "string", format = "binary"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @Parameters({
      @Parameter(name = "bizNm", description = "법인명 (부분 일치)", example = "삼성", in = ParameterIn.QUERY),
      @Parameter(name = "bizNo", description = "사업자번호 (하이픈 포함/제외 모두 가능)", example = "124-81-00998", in = ParameterIn.QUERY),
      @Parameter(name = "sellerId", description = "판매자 ID (부분 일치)", example = "seller123", in = ParameterIn.QUERY),
      @Parameter(name = "corpRegNo", description = "법인등록번호", example = "110111-1234567", in = ParameterIn.QUERY),
      @Parameter(name = "city", description = "시/도", example = "서울특별시", in = ParameterIn.QUERY),
      @Parameter(name = "district", description = "구/군", example = "강남구", in = ParameterIn.QUERY)
  })
  @GetMapping("/export")
  public void exportToExcel(
      CorpMastManualRequest request,
      HttpServletResponse response,
      @Parameter(hidden = true) @CurrentUser AuthenticatedUser user) throws Exception {

    log.info("엑셀 내보내기 요청: {}", request);

    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"corp_data.xlsx\"");

    excelService.exportToExcel(request, response.getOutputStream());
  }

  @Operation(summary = "법인 정보 검색", description = """
      다양한 조건으로 법인 정보를 검색합니다. 페이지네이션 파라미터가 제공되면 페이지네이션을 적용합니다.

      ### 검색 조건
      - **법인명**: 부분 일치 검색 (대소문자 무관)
      - **사업자번호**: 정확 일치 (하이픈 포함/제외 모두 가능)
      - **판매자 ID**: 부분 일치 검색 (대소문자 무관)
      - **법인등록번호**: 정확 일치
      - **지역**: 시/도 및 구/군 조합

      ### 페이징
      - 기본 페이지 크기: 10개
      - 최대 페이지 크기: 100개
      - 정렬: ID 내림차순 기본
      - 페이지네이션 파라미터가 기본값이 아니면 페이징 적용

      ### 캐싱
      - 동일한 검색 조건은 5분간 캐시됩니다
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "법인 검색 성공 응답", value = """
          {
            "success": true,
            "message": "검색 완료",
            "data": {
              "content": [
                {
                  "id": 1,
                  "sellerId": "seller123",
                  "bizNm": "삼성전자주식회사",
                  "bizNo": "124-81-00998",
                  "corpRegNo": "110111-1234567",
                  "regionCd": "11680",
                  "siNm": "서울특별시",
                  "sggNm": "강남구",
                  "username": "admin",
                  "description": "전자제품 제조업"
                }
              ],
              "pageable": {
                "pageNumber": 0,
                "pageSize": 10
              },
              "totalElements": 150,
              "totalPages": 15,
              "first": true,
              "last": false
            },
            "timestamp": "2024-01-15T10:30:00"
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 검색 조건", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @Parameters({
      @Parameter(ref = "#/components/parameters/pageParam"),
      @Parameter(ref = "#/components/parameters/sizeParam"),
      @Parameter(ref = "#/components/parameters/sortParam"),
      @Parameter(name = "bizNm", description = "법인명 (부분 일치)", example = "삼성", in = ParameterIn.QUERY),
      @Parameter(name = "bizNo", description = "사업자번호 (하이픈 포함/제외 모두 가능)", example = "124-81-00998", in = ParameterIn.QUERY),
      @Parameter(name = "sellerId", description = "판매자 ID (부분 일치)", example = "seller123", in = ParameterIn.QUERY),
      @Parameter(name = "corpRegNo", description = "법인등록번호", example = "110111-1234567", in = ParameterIn.QUERY),
      @Parameter(name = "city", description = "시/도", example = "서울특별시", in = ParameterIn.QUERY),
      @Parameter(name = "district", description = "구/군", example = "강남구", in = ParameterIn.QUERY)
  })
  @GetMapping("/search")
  public ApiResponse<?> search(
      @ModelAttribute CorpMastManualRequest request,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "desc") String sortDir) {

    if (page > 0 || size != 10) {
      Page<CorpMastSearchResponse> paginatedResult = corpMastManualService.searchWithPagination(request, page, size,
          sortBy, sortDir);
      return ApiResponse.of(HttpStatus.OK, "검색 완료", paginatedResult);
    } else {
      List<CorpMastSearchResponse> result = corpMastManualService.search(request);
      return ApiResponse.of(HttpStatus.OK, "검색 완료", result);
    }
  }

  @Operation(summary = "법인 정보 상세 조회", description = """
      ID를 이용하여 특정 법인의 상세 정보를 조회합니다.

      ### 기능
      - 법인 ID로 단일 법인 정보 조회
      - 모든 법인 정보 필드 반환
      - 캐시된 결과 반환 (5분)

      ### 응답 정보
      - 법인 기본 정보 (법인명, 사업자번호, 법인등록번호)
      - 지역 정보 (시/도, 구/군, 지역코드)
      - 관리 정보 (등록자, 설명)
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "법인 상세 조회 성공", value = """
          {
            "success": true,
            "message": "요청 처리가 완료되었습니다",
            "data": {
              "id": 1,
              "sellerId": "seller123",
              "bizNm": "삼성전자주식회사",
              "bizNo": "124-81-00998",
              "corpRegNo": "110111-1234567",
              "regionCd": "11680",
              "siNm": "서울특별시",
              "sggNm": "강남구",
              "username": "admin",
              "description": "전자제품 제조업"
            },
            "timestamp": "2024-01-15T10:30:00"
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "법인 정보를 찾을 수 없음", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"), examples = @ExampleObject(name = "법인 정보 없음", value = """
          {
            "success": false,
            "message": "법인 정보를 찾을 수 없습니다.",
            "data": null,
            "timestamp": "2024-01-15T10:30:00"
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 ID 형식", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping("/{id}")
  public ApiResponse<CorpMastManualResponse> getById(
      @Parameter(description = "법인 ID", example = "1", required = true) @PathVariable Long id) {
    log.info("법인 정보 조회 요청 - ID: {}", id);

    try {
      CorpMastManualResponse result = corpMastManualService.getById(id);
      return ApiResponse.success(result);
    } catch (Exception e) {
      log.error("법인 정보 조회 실패 - ID: {}", id, e);
      return ApiResponse.error("법인 정보를 찾을 수 없습니다.");
    }
  }

  @Operation(summary = "사업자번호로 법인 정보 조회", description = """
      사업자번호를 이용하여 법인 정보를 조회합니다.

      ### 기능
      - 사업자번호로 정확한 법인 정보 조회
      - 하이픈 포함/제외 모두 지원 (124-81-00998 또는 1248100998)
      - 캐시된 결과 반환 (5분)

      ### 사업자번호 형식
      - 10자리 숫자 (1248100998)
      - 하이픈 포함 13자리 (124-81-00998)
      - URL 인코딩된 형태도 지원
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "사업자번호 조회 성공", value = """
          {
            "success": true,
            "message": "요청 처리가 완료되었습니다",
            "data": {
              "id": 1,
              "sellerId": "seller123",
              "bizNm": "삼성전자주식회사",
              "bizNo": "124-81-00998",
              "corpRegNo": "110111-1234567",
              "regionCd": "11680",
              "siNm": "서울특별시",
              "sggNm": "강남구",
              "username": "admin",
              "description": "전자제품 제조업"
            },
            "timestamp": "2024-01-15T10:30:00"
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사업자번호에 해당하는 법인 정보를 찾을 수 없음", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 사업자번호 형식", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping("/bizno/{bizNo}")
  public ApiResponse<CorpMastManualResponse> getByBizNo(
      @Parameter(description = "사업자번호 (하이픈 포함/제외 모두 가능)", example = "124-81-00998", required = true) @PathVariable String bizNo) {
    log.info("사업자번호로 법인 정보 조회 요청: {}", bizNo);

    try {
      CorpMastManualResponse result = corpMastManualService.getByBizNo(bizNo);
      return ApiResponse.success(result);
    } catch (Exception e) {
      log.error("사업자번호로 법인 정보 조회 실패: {}", bizNo, e);
      return ApiResponse.error("해당 사업자번호의 법인 정보를 찾을 수 없습니다.");
    }
  }

  @Operation(summary = "법인등록번호로 법인 정보 조회", description = """
      법인등록번호를 이용하여 법인 정보를 조회합니다.

      ### 기능
      - 법인등록번호로 정확한 법인 정보 조회
      - 13자리 법인등록번호 형식 지원
      - 캐시된 결과 반환 (5분)

      ### 법인등록번호 형식
      - 13자리 하이픈 포함 (110111-1234567)
      - 13자리 하이픈 없음 (1101111234567)
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "법인등록번호에 해당하는 법인 정보를 찾을 수 없음", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 법인등록번호 형식", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping("/regno/{corpRegNo}")
  public ApiResponse<CorpMastManualResponse> getByCorpRegNo(
      @Parameter(description = "법인등록번호 (13자리)", example = "110111-1234567", required = true) @PathVariable String corpRegNo) {
    log.info("법인등록번호로 법인 정보 조회 요청: {}", corpRegNo);

    try {
      CorpMastManualResponse result = corpMastManualService.getByCorpRegNo(corpRegNo);
      return ApiResponse.success(result);
    } catch (Exception e) {
      log.error("법인등록번호로 법인 정보 조회 실패: {}", corpRegNo, e);
      return ApiResponse.error("해당 법인등록번호의 법인 정보를 찾을 수 없습니다.");
    }
  }

  @Operation(summary = "시/도 목록 조회", description = """
      등록된 법인들의 시/도 목록을 조회합니다.

      ### 기능
      - 법인 데이터에서 추출한 고유한 시/도 목록
      - 알파벳 순으로 정렬된 결과
      - 캐시된 결과 반환 (5분)

      ### 활용
      - 검색 필터의 시/도 선택 옵션으로 활용
      - 지역별 통계 생성 시 기준 데이터로 활용
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "시/도 목록", value = """
          {
            "success": true,
            "message": "요청 처리가 완료되었습니다",
            "data": [
              "강원특별자치도",
              "경기도",
              "경상남도",
              "경상북도",
              "광주광역시",
              "대구광역시",
              "대전광역시",
              "부산광역시",
              "서울특별시",
              "세종특별자치시",
              "울산광역시",
              "인천광역시",
              "전라남도",
              "전라북도",
              "제주특별자치도",
              "충청남도",
              "충청북도"
            ],
            "timestamp": "2024-01-15T10:30:00"
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping("/cities")
  public ApiResponse<List<String>> getCities() {
    log.info("도시 목록 조회 요청");

    try {
      List<String> cities = corpMastManualService.getAllCities();
      return ApiResponse.success(cities);
    } catch (Exception e) {
      log.error("도시 목록 조회 실패", e);
      return ApiResponse.error("도시 목록을 가져올 수 없습니다.");
    }
  }

  @Operation(summary = "특정 시/도의 구/군 목록 조회", description = """
      특정 시/도에 속한 구/군 목록을 조회합니다.

      ### 기능
      - 특정 시/도에 등록된 법인들의 구/군 목록
      - 알파벳 순으로 정렬된 결과
      - 캐시된 결과 반환 (5분)

      ### 활용
      - 지역 필터의 2단계 선택 옵션으로 활용
      - 시/도 선택 후 세부 지역 선택 시 사용

      ### 주의사항
      - 존재하지 않는 시/도의 경우 빈 배열 반환
      - URL 인코딩된 시/도명도 지원
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "서울특별시 구/군 목록", value = """
          {
            "success": true,
            "message": "요청 처리가 완료되었습니다",
            "data": [
              "강남구",
              "강동구",
              "강북구",
              "강서구",
              "관악구",
              "광진구",
              "구로구",
              "금천구",
              "노원구",
              "도봉구",
              "동대문구",
              "동작구",
              "마포구",
              "서대문구",
              "서초구",
              "성동구",
              "성북구",
              "송파구",
              "양천구",
              "영등포구",
              "용산구",
              "은평구",
              "종로구",
              "중구",
              "중랑구"
            ],
            "timestamp": "2024-01-15T10:30:00"
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping("/districts/{city}")
  public ApiResponse<List<String>> getDistrictsByCity(
      @Parameter(description = "시/도명 (정확한 명칭 필요)", example = "서울특별시", required = true) @PathVariable String city) {
    log.info("구/군 목록 조회 요청 - 도시: {}", city);

    try {
      List<String> districts = corpMastManualService.getDistrictsByCity(city);
      return ApiResponse.success(districts);
    } catch (Exception e) {
      log.error("구/군 목록 조회 실패 - 도시: {}", city, e);
      return ApiResponse.error("해당 도시의 구/군 목록을 가져올 수 없습니다.");
    }
  }

  @Operation(summary = "법인 검색 통계 정보 조회", description = """
      법인 검색 조건에 따른 통계 정보를 조회합니다.

      ### 제공 통계
      - **총 법인 수**: 검색 조건에 맞는 전체 법인 수
      - **지역별 분포**: 시/도 및 구/군별 법인 수
      - **법인등록번호 유효성**: 유효한 법인등록번호 보유 법인 수
      - **지역코드 유효성**: 유효한 지역코드 보유 법인 수

      ### 활용
      - 대시보드 차트 데이터 생성
      - 데이터 품질 확인
      - 지역별 사업자 분포 파악

      ### 캐싱
      - 동일한 검색 조건은 5분간 캐시됩니다
      """, tags = { "CorpMast Management" })
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "통계 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(name = "검색 통계 정보", value = """
          {
            "success": true,
            "message": "요청 처리가 완료되었습니다",
            "data": {
              "totalCount": 1500,
              "validCorpRegNoCount": 1350,
              "validRegionCdCount": 1400,
              "completionRate": 90.0,
              "regionDistribution": {
                "서울특별시": {
                  "강남구": 120,
                  "서초구": 85,
                  "중구": 95
                },
                "경기도": {
                  "수원시": 70,
                  "성남시": 60
                }
              },
              "searchConditions": {
                "bizNm": "삼성",
                "city": "서울특별시"
              }
            },
            "timestamp": "2024-01-15T10:30:00"
          }
          """))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @Parameters({
      @Parameter(name = "bizNm", description = "법인명 (부분 일치)", example = "삼성", in = ParameterIn.QUERY),
      @Parameter(name = "bizNo", description = "사업자번호", example = "124-81-00998", in = ParameterIn.QUERY),
      @Parameter(name = "sellerId", description = "판매자 ID", example = "seller123", in = ParameterIn.QUERY),
      @Parameter(name = "corpRegNo", description = "법인등록번호", example = "110111-1234567", in = ParameterIn.QUERY),
      @Parameter(name = "city", description = "시/도", example = "서울특별시", in = ParameterIn.QUERY),
      @Parameter(name = "district", description = "구/군", example = "강남구", in = ParameterIn.QUERY)
  })
  @GetMapping("/statistics")
  public ApiResponse<Map<String, Object>> getSearchStatistics(
      @Parameter(description = "검색 조건 (통계 생성 기준)") @ModelAttribute CorpMastManualRequest searchRequest) {

    log.info("검색 통계 요청: {}", searchRequest);

    try {
      Map<String, Object> statistics = corpMastManualService.getSearchStatistics(searchRequest);
      return ApiResponse.success(statistics);
    } catch (Exception e) {
      log.error("검색 통계 조회 실패", e);
      return ApiResponse.error("검색 통계를 가져올 수 없습니다.");
    }
  }

}