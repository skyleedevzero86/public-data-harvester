package com.antock.corp.application.dto;

import com.antock.api.corpmanual.application.dto.request.CorpMastManualRequest;
import com.antock.api.corpmanual.application.dto.response.CorpMastManualResponse;
import com.antock.api.coseller.domain.CorpMast;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CorpMast DTO 테스트")
class CorpMastDtoTest {

    @Test
    @DisplayName("CorpMastSearchRequest - 검색 조건 정리 테스트")
    void corpMastSearchRequest_ShouldCleanSearchConditions() {
        // given
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setBizNm("  주식회사 테스트  ");
        request.setBizNo("140-81-99474");
        request.setSellerId("  2025-서울강남-01714  ");
        request.setCorpRegNo("  1101110918053  ");
        request.setCity("  서울특별시  ");
        request.setDistrict("  강남구  ");

        // when & then
        assertThat(request.getBizNmForSearch()).isEqualTo("주식회사 테스트");
        assertThat(request.getBizNoForSearch()).isEqualTo("14081999474");
        assertThat(request.getSellerIdForSearch()).isEqualTo("2025-서울강남-01714");
        assertThat(request.getCorpRegNoForSearch()).isEqualTo("1101110918053");
        assertThat(request.getCityForSearch()).isEqualTo("서울특별시");
        assertThat(request.getDistrictForSearch()).isEqualTo("강남구");
    }

    @Test
    @DisplayName("CorpMastSearchRequest - null 값 처리")
    void corpMastSearchRequest_WithNullValues_ShouldReturnNull() {
        // given
        CorpMastManualRequest request = new CorpMastManualRequest();

        // when & then
        assertThat(request.getBizNmForSearch()).isNull();
        assertThat(request.getBizNoForSearch()).isNull();
        assertThat(request.getSellerIdForSearch()).isNull();
        assertThat(request.getCorpRegNoForSearch()).isNull();
        assertThat(request.getCityForSearch()).isNull();
        assertThat(request.getDistrictForSearch()).isNull();
    }

    @Test
    @DisplayName("CorpMastSearchRequest - 빈 문자열 처리")
    void corpMastSearchRequest_WithEmptyStrings_ShouldReturnNull() {
        // given
        CorpMastManualRequest request = new CorpMastManualRequest();
        request.setBizNm("");
        request.setBizNo("   ");
        request.setSellerId("");

        // when & then
        assertThat(request.getBizNmForSearch()).isEmpty();
        assertThat(request.getBizNoForSearch()).isEmpty();
        assertThat(request.getSellerIdForSearch()).isEmpty();
    }

    @Test
    @DisplayName("CorpMastSearchRequest - 검색 조건 존재 여부 확인")
    void corpMastSearchRequest_HasSearchCondition_ShouldWork() {
        // given
        CorpMastManualRequest emptyRequest = new CorpMastManualRequest();

        CorpMastManualRequest requestWithBizNm = new CorpMastManualRequest();
        requestWithBizNm.setBizNm("테스트");

        CorpMastManualRequest requestWithEmptyStrings = new CorpMastManualRequest();
        requestWithEmptyStrings.setBizNm("");
        requestWithEmptyStrings.setBizNo("   ");

        // when & then
        assertThat(emptyRequest.hasSearchCondition()).isFalse();
        assertThat(requestWithBizNm.hasSearchCondition()).isTrue();
        assertThat(requestWithEmptyStrings.hasSearchCondition()).isFalse();
    }

    @Test
    @DisplayName("CorpMastSearchRequest - 기본값 설정")
    void corpMastSearchRequest_DefaultValues_ShouldBeSet() {
        // given
        CorpMastManualRequest request = new CorpMastManualRequest();

        // when & then
        assertThat(request.getPage()).isEqualTo(0);
        assertThat(request.getSize()).isEqualTo(20);
        assertThat(request.getSort()).isEqualTo("id,desc");
    }

    @Test
    @DisplayName("CorpMastResponse - Entity에서 DTO 변환")
    void corpMastResponse_FromEntity_ShouldConvertCorrectly() {
        // given
        CorpMast entity = CorpMast.builder()
                .id(1L)
                .sellerId("2025-서울강남-01714")
                .bizNm("주식회사 뮤직턴")
                .bizNo("140-81-99474")
                .corpRegNo("1101110918053")
                .regionCd("1168010100")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("admin")
                .build();

        // when
        CorpMastManualResponse response = CorpMastManualResponse.from(entity);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSellerId()).isEqualTo("2025-서울강남-01714");
        assertThat(response.getBizNm()).isEqualTo("주식회사 뮤직턴");
        assertThat(response.getBizNo()).isEqualTo("140-81-99474");
        assertThat(response.getCorpRegNo()).isEqualTo("1101110918053");
        assertThat(response.getRegionCd()).isEqualTo("1168010100");
        assertThat(response.getSiNm()).isEqualTo("서울특별시");
        assertThat(response.getSggNm()).isEqualTo("강남구");
        assertThat(response.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("CorpMastResponse - 포맷된 사업자번호")
    void corpMastResponse_FormattedBizNo_ShouldFormatCorrectly() {
        // given
        CorpMast entity = CorpMast.builder()
                .bizNo("140-81-99474")
                .build();

        // when
        CorpMastManualResponse response = CorpMastManualResponse.from(entity);

        // then
        assertThat(response.getFormattedBizNo()).isEqualTo("140-81-99474");
    }

    @Test
    @DisplayName("CorpMastResponse - 하이픈 없는 사업자번호 포맷팅")
    void corpMastResponse_FormattedBizNo_WithoutHyphen_ShouldAddHyphens() {
        // given
        CorpMast entity = CorpMast.builder()
                .bizNo("1408199474")
                .build();

        // when
        CorpMastManualResponse response = CorpMastManualResponse.from(entity);

        // then
        assertThat(response.getFormattedBizNo()).isEqualTo("140-81-9474");
    }

    @Test
    @DisplayName("CorpMastResponse - 잘못된 길이의 사업자번호")
    void corpMastResponse_FormattedBizNo_WithInvalidLength_ShouldReturnOriginal() {
        // given
        CorpMast entity = CorpMast.builder()
                .bizNo("12345")
                .build();

        // when
        CorpMastManualResponse response = CorpMastManualResponse.from(entity);

        // then
        assertThat(response.getFormattedBizNo()).isEqualTo("12345");
    }

    @Test
    @DisplayName("CorpMastResponse - null 사업자번호")
    void corpMastResponse_FormattedBizNo_WithNull_ShouldReturnNull() {
        // given
        CorpMast entity = CorpMast.builder()
                .bizNo(null)
                .build();

        // when
        CorpMastManualResponse response = CorpMastManualResponse.from(entity);

        // then
        assertThat(response.getFormattedBizNo()).isNull();
    }

    @Test
    @DisplayName("CorpMastResponse - 전체 주소 조합")
    void corpMastResponse_FullAddress_ShouldCombineCorrectly() {
        // given
        CorpMast entity = CorpMast.builder()
                .siNm("서울특별시")
                .sggNm("강남구")
                .build();

        // when
        CorpMastManualResponse response = CorpMastManualResponse.from(entity);

        // then
        assertThat(response.getFullAddress()).isEqualTo("서울특별시 강남구");
    }

    @Test
    @DisplayName("CorpMastResponse - Builder 패턴 테스트")
    void corpMastResponse_Builder_ShouldWork() {
        // when
        CorpMastManualResponse response = CorpMastManualResponse.builder()
                .id(1L)
                .bizNm("테스트 법인")
                .bizNo("123-45-67890")
                .siNm("서울특별시")
                .sggNm("강남구")
                .build();

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBizNm()).isEqualTo("테스트 법인");
        assertThat(response.getBizNo()).isEqualTo("123-45-67890");
        assertThat(response.getFullAddress()).isEqualTo("서울특별시 강남구");
    }

    @Test
    @DisplayName("CorpMastSearchRequest - 사업자번호 하이픈 제거 확인")
    void corpMastSearchRequest_BizNoForSearch_ShouldRemoveHyphens() {
        // given
        CorpMastManualRequest request = new CorpMastManualRequest();

        // when & then
        request.setBizNo("140-81-99474");
        assertThat(request.getBizNoForSearch()).isEqualTo("14081999474");

        request.setBizNo("140-81-99-474");
        assertThat(request.getBizNoForSearch()).isEqualTo("1408199474");

        request.setBizNo("1408199474");
        assertThat(request.getBizNoForSearch()).isEqualTo("1408199474");
    }

    @Test
    @DisplayName("CorpMastSearchRequest - 모든 생성자 테스트")
    void corpMastSearchRequest_Constructors_ShouldWork() {
        // given & when
        CorpMastManualRequest defaultRequest = new CorpMastManualRequest();
        CorpMastManualRequest allArgsRequest = new CorpMastManualRequest(
                "테스트법인", "123-45-67890", "seller123", "corp123",
                "서울특별시", "강남구", 1, 10, "bizNm,asc"
        );

        // then
        assertThat(defaultRequest.getBizNm()).isNull();
        assertThat(defaultRequest.getPage()).isEqualTo(0);
        assertThat(defaultRequest.getSize()).isEqualTo(20);

        assertThat(allArgsRequest.getBizNm()).isEqualTo("테스트법인");
        assertThat(allArgsRequest.getBizNo()).isEqualTo("123-45-67890");
        assertThat(allArgsRequest.getPage()).isEqualTo(1);
        assertThat(allArgsRequest.getSize()).isEqualTo(10);
        assertThat(allArgsRequest.getSort()).isEqualTo("bizNm,asc");
    }
}