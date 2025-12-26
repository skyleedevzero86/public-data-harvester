package com.antock.api.dashboard.application.service;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionStatService 테스트")
class RegionStatServiceTest {

    @Mock
    private CorpMastRepository corpMastRepository;

    @InjectMocks
    private RegionStatService regionStatService;

    @BeforeEach
    void setUp() {
        Object[] statData = new Object[]{"서울특별시", "강남구", 100L, 80L, 75L};
        List<Object[]> statsList = new ArrayList<>();
        statsList.add(statData);
        when(corpMastRepository.getRegionStats()).thenReturn(statsList);
    }

    @Test
    @DisplayName("상위 지역 통계 조회")
    void getTopRegionStat() {
        com.antock.api.dashboard.application.dto.RegionStatDto result = regionStatService.getTopRegionStat();

        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("서울특별시");
        assertThat(result.getDistrict()).isEqualTo("강남구");
    }

    @Test
    @DisplayName("모든 지역 통계 조회")
    void getAllRegionStats() {
        List<com.antock.api.dashboard.application.dto.RegionStatDto> result = regionStatService.getAllRegionStats();

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("페이징된 지역 통계 조회")
    void getRegionStatsWithPaging() {
        Object[] statData = new Object[]{"서울특별시", "강남구", 100L, 80L, 75L};
        List<Object[]> content = new ArrayList<>();
        content.add(statData);
        Page<Object[]> page = new PageImpl<>(content);
        Pageable pageable = PageRequest.of(0, 10);

        when(corpMastRepository.getRegionStatsWithPaging(any(Pageable.class), any(), any()))
                .thenReturn(page);

        Page<com.antock.api.dashboard.application.dto.RegionStatDto> result =
                regionStatService.getRegionStatsWithPaging(pageable, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("도시 목록 조회")
    void getCities() {
        when(corpMastRepository.findDistinctCities()).thenReturn(Arrays.asList("서울특별시", "부산광역시"));

        List<String> cities = regionStatService.getCities();

        assertThat(cities).hasSize(2);
    }

    @Test
    @DisplayName("도시별 구/군 목록 조회")
    void getDistrictsByCity() {
        when(corpMastRepository.findDistinctDistrictsByCity("서울특별시"))
                .thenReturn(Arrays.asList("강남구", "강동구"));

        List<String> districts = regionStatService.getDistrictsByCity("서울특별시");

        assertThat(districts).hasSize(2);
    }

    @Test
    @DisplayName("빈 통계 조회")
    void getTopRegionStat_empty() {
        when(corpMastRepository.getRegionStats()).thenReturn(Arrays.asList());

        com.antock.api.dashboard.application.dto.RegionStatDto result = regionStatService.getTopRegionStat();

        assertThat(result).isNull();
    }
}

