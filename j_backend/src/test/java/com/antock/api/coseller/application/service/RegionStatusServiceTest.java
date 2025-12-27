package com.antock.api.coseller.application.service;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionStatusService 테스트")
class RegionStatusServiceTest {

    @Mock
    private CorpMastRepository corpMastRepository;

    @InjectMocks
    private RegionStatusService regionStatusService;

    @Test
    @DisplayName("지역별 법인 목록 조회 성공 - 시/도와 구/군")
    void getCorpMastList_WithCityAndDistrict() {
        CorpMast corp1 = CorpMast.builder()
                .sellerId("seller001")
                .bizNm("법인1")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        List<CorpMast> corps = Arrays.asList(corp1);
        given(corpMastRepository.findBySiNmAndSggNm(anyString(), anyString())).willReturn(corps);

        List<CorpMast> result = regionStatusService.getCorpMastList("서울특별시", "강남구");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSiNm()).isEqualTo("서울특별시");
    }

    @Test
    @DisplayName("지역별 법인 목록 조회 성공 - 시/도만")
    void getCorpMastList_WithCityOnly() {
        CorpMast corp1 = CorpMast.builder()
                .sellerId("seller001")
                .bizNm("법인1")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        List<CorpMast> corps = Arrays.asList(corp1);
        given(corpMastRepository.findBySiNm(anyString())).willReturn(corps);

        List<CorpMast> result = regionStatusService.getCorpMastList("서울특별시", null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("구/군 목록 조회 성공")
    void getDistrictsByCity_Success() {
        List<String> districts = regionStatusService.getDistrictsByCity("서울특별시");

        assertThat(districts).isNotEmpty();
    }
}

