package com.antock.corp.infrastructure;

import com.antock.api.corpsearch.infrastructure.CorpMastSearchRepository;
import com.antock.api.coseller.domain.CorpMast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CorpMastRepository 테스트")
public class CorpMastSearchRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CorpMastSearchRepository corpMastRepository;

    private CorpMast testCorp1;
    private CorpMast testCorp2;
    private CorpMast testCorp3;

    @BeforeEach
    void setUp() {
        testCorp1 = CorpMast.builder()
                .sellerId("2025-서울강남-01714")
                .bizNm("주식회사 뮤직턴")
                .bizNo("140-81-99474")
                .corpRegNo("1101110918053")
                .regionCd("1168010100")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("admin")
                .build();

        testCorp2 = CorpMast.builder()
                .sellerId("2025-서울강남-01726")
                .bizNm("주식회사 뷰타민")
                .bizNo("510-86-03231")
                .corpRegNo("1101110932733")
                .regionCd("1168010100")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("admin")
                .build();

        testCorp3 = CorpMast.builder()
                .sellerId("2025-부산해운-01001")
                .bizNm("부산물류주식회사")
                .bizNo("220-88-12345")
                .corpRegNo("2101110111111")
                .regionCd("2600010100")
                .siNm("부산광역시")
                .sggNm("해운대구")
                .username("manager")
                .build();

        entityManager.persistAndFlush(testCorp1);
        entityManager.persistAndFlush(testCorp2);
        entityManager.persistAndFlush(testCorp3);
    }

    @Test
    @DisplayName("법인명으로 검색 테스트")
    void findBySearchConditions_WithBizNm_ShouldReturnMatchingCorps() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                "뮤직턴", null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBizNm()).contains("뮤직턴");
    }

    @Test
    @DisplayName("사업자번호로 검색 테스트")
    void findBySearchConditions_WithBizNo_ShouldReturnMatchingCorp() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                null, "14081999474", null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBizNo()).isEqualTo("140-81-99474");
    }

    @Test
    @DisplayName("지역으로 검색 테스트")
    void findBySearchConditions_WithLocation_ShouldReturnMatchingCorps() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                null, null, null, null, "서울특별시", "강남구", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(corp ->
                corp.getSiNm().equals("서울특별시") && corp.getSggNm().equals("강남구"));
    }

    @Test
    @DisplayName("판매자ID로 검색 테스트")
    void findBySearchConditions_WithSellerId_ShouldReturnMatchingCorps() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                null, null, "서울강남", null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(corp ->
                corp.getSellerId().contains("서울강남"));
    }

    @Test
    @DisplayName("복합 조건 검색 테스트")
    void findBySearchConditions_WithMultipleConditions_ShouldReturnMatchingCorps() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                "주식회사", null, null, null, "서울특별시", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(corp ->
                corp.getBizNm().contains("주식회사") && corp.getSiNm().equals("서울특별시"));
    }

    @Test
    @DisplayName("조건이 없을 때 전체 조회")
    void findBySearchConditions_WithoutConditions_ShouldReturnAllCorps() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                null, null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("사업자번호로 단일 조회 테스트")
    void findByBizNo_WithValidBizNo_ShouldReturnCorp() {
        // when
        Optional<CorpMast> result = corpMastRepository.findByBizNo("140-81-99474");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getBizNm()).isEqualTo("주식회사 뮤직턴");
    }

    @Test
    @DisplayName("존재하지 않는 사업자번호로 조회 시 빈 결과")
    void findByBizNo_WithInvalidBizNo_ShouldReturnEmpty() {
        // when
        Optional<CorpMast> result = corpMastRepository.findByBizNo("000-00-00000");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("법인등록번호로 단일 조회 테스트")
    void findByCorpRegNo_WithValidCorpRegNo_ShouldReturnCorp() {
        // when
        Optional<CorpMast> result = corpMastRepository.findByCorpRegNo("1101110918053");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getBizNm()).isEqualTo("주식회사 뮤직턴");
    }

    @Test
    @DisplayName("지역별 법인 수 조회 테스트")
    void countByLocation_WithValidLocation_ShouldReturnCount() {
        // when
        long count = corpMastRepository.countByLocation("서울특별시", "강남구");

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("시/도만으로 법인 수 조회 테스트")
    void countByLocation_WithCityOnly_ShouldReturnCount() {
        // when
        long count = corpMastRepository.countByLocation("서울특별시", null);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("판매자ID로 페이징 조회 테스트")
    void findBySellerIdContainingIgnoreCase_ShouldReturnPagedResult() {
        // given
        Pageable pageable = PageRequest.of(0, 1);

        // when
        Page<CorpMast> result = corpMastRepository.findBySellerIdContainingIgnoreCase("서울강남", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("법인명으로 페이징 조회 테스트")
    void findByBizNmContainingIgnoreCase_ShouldReturnPagedResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findByBizNmContainingIgnoreCase("주식회사", pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).allMatch(corp -> corp.getBizNm().contains("주식회사"));
    }

    @Test
    @DisplayName("시/도별 구/군 목록 조회 테스트")
    void findDistinctDistrictsByCity_WithValidCity_ShouldReturnDistricts() {
        // when
        List<String> districts = corpMastRepository.findDistinctDistrictsByCity("서울특별시");

        // then
        assertThat(districts).hasSize(1);
        assertThat(districts).contains("강남구");
    }

    @Test
    @DisplayName("전체 시/도 목록 조회 테스트")
    void findDistinctCities_ShouldReturnAllCities() {
        // when
        List<String> cities = corpMastRepository.findDistinctCities();

        // then
        assertThat(cities).hasSize(2);
        assertThat(cities).contains("서울특별시", "부산광역시");
    }

    @Test
    @DisplayName("대소문자 무관 검색 테스트")
    void findBySearchConditions_CaseInsensitiveSearch_ShouldWork() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<CorpMast> result = corpMastRepository.findBySearchConditions(
                "MUSIC", null, null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBizNm()).contains("뮤직턴");
    }

    @Test
    @DisplayName("페이징 테스트")
    void findBySearchConditions_WithPaging_ShouldReturnCorrectPage() {
        // given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // when
        Page<CorpMast> firstResult = corpMastRepository.findBySearchConditions(
                null, null, null, null, null, null, firstPage);
        Page<CorpMast> secondResult = corpMastRepository.findBySearchConditions(
                null, null, null, null, null, null, secondPage);

        // then
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(secondResult.getContent()).hasSize(1);
        assertThat(firstResult.getTotalElements()).isEqualTo(3);
        assertThat(secondResult.getTotalElements()).isEqualTo(3);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(secondResult.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("하이픈 포함/제외 사업자번호 검색 테스트")
    void findByBizNo_WithOrWithoutHyphen_ShouldWork() {
        // when
        Optional<CorpMast> resultWithHyphen = corpMastRepository.findByBizNo("140-81-99474");
        Optional<CorpMast> resultWithoutHyphen = corpMastRepository.findByBizNo("14081999474");

        // then
        assertThat(resultWithHyphen).isPresent();
        assertThat(resultWithoutHyphen).isPresent();
        assertThat(resultWithHyphen.get().getId()).isEqualTo(resultWithoutHyphen.get().getId());
    }
}