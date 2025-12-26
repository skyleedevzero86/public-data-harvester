package com.antock.api.coseller.application.service;

import com.antock.api.coseller.application.dto.CorpMastCreateDTO;
import com.antock.api.coseller.application.dto.api.BizCsvInfoDto;
import com.antock.api.coseller.application.dto.api.RegionInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoSellerDataMapper 테스트")
class CoSellerDataMapperTest {

    @InjectMocks
    private CoSellerDataMapper coSellerDataMapper;

    private BizCsvInfoDto bizCsvInfoDto;
    private RegionInfoDto regionInfoDto;

    @BeforeEach
    void setUp() {
        bizCsvInfoDto = BizCsvInfoDto.builder()
                .sellerId("seller1")
                .bizNm("테스트 법인")
                .bizNo("123-45-67890")
                .ownerName("홍길동")
                .date("20200101")
                .bizAddress("서울특별시 강남구")
                .bizNesAddress("서울특별시 강남구")
                .build();

        regionInfoDto = RegionInfoDto.builder()
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .build();
    }

    @Test
    @DisplayName("CSV 리스트를 DTO 리스트로 변환")
    void mapToCorpMastCreateDTO() {
        List<BizCsvInfoDto> csvList = Arrays.asList(bizCsvInfoDto);

        List<CorpMastCreateDTO> result = coSellerDataMapper.mapToCorpMastCreateDTO(csvList, "testuser");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getBizNm()).isEqualTo("테스트 법인");
    }

    @Test
    @DisplayName("도시와 구/군이 있는 경우 변환")
    void mapToCorpMastCreateDTO_withCityDistrict() {
        List<BizCsvInfoDto> csvList = Arrays.asList(bizCsvInfoDto);

        List<CorpMastCreateDTO> result = coSellerDataMapper.mapToCorpMastCreateDTO(
                csvList, "testuser", "서울특별시", "강남구");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getSiNm()).isEqualTo("서울특별시");
        assertThat(result.get(0).getSggNm()).isEqualTo("강남구");
    }

    @Test
    @DisplayName("API 데이터로부터 DTO 생성 - 유효한 데이터")
    void mapFromApiData_valid() {
        Optional<CorpMastCreateDTO> result = coSellerDataMapper.mapFromApiData(
                bizCsvInfoDto, "1234567890123", regionInfoDto, "testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getBizNm()).isEqualTo("테스트 법인");
        assertThat(result.get().getCorpRegNo()).isEqualTo("1234567890123");
        assertThat(result.get().getRegionCd()).isEqualTo("11680");
    }

    @Test
    @DisplayName("API 데이터로부터 DTO 생성 - 유효하지 않은 법인등록번호")
    void mapFromApiData_invalidCorpRegNo() {
        Optional<CorpMastCreateDTO> result = coSellerDataMapper.mapFromApiData(
                bizCsvInfoDto, "0", regionInfoDto, "testuser");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("API 데이터로부터 DTO 생성 - 유효하지 않은 지역정보")
    void mapFromApiData_invalidRegionInfo() {
        RegionInfoDto invalidRegion = RegionInfoDto.builder()
                .regionCd("0")
                .build();

        Optional<CorpMastCreateDTO> result = coSellerDataMapper.mapFromApiData(
                bizCsvInfoDto, "1234567890123", invalidRegion, "testuser");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 리스트 변환")
    void mapToCorpMastCreateDTO_empty() {
        List<BizCsvInfoDto> csvList = Arrays.asList();

        List<CorpMastCreateDTO> result = coSellerDataMapper.mapToCorpMastCreateDTO(csvList, "testuser");

        assertThat(result).isEmpty();
    }
}

