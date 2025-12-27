package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CorpMastRepository 테스트")
class CorpMastRepositoryTest {

    @Autowired
    private CorpMastRepository corpMastRepository;

    @Test
    @DisplayName("사업자번호로 법인 조회 성공")
    void findByBizNo_Success() {
        CorpMast corp = CorpMast.builder()
                .sellerId("seller001")
                .bizNm("테스트법인")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        corpMastRepository.save(corp);

        Optional<CorpMast> found = corpMastRepository.findByBizNo("123-45-67890");
        assertThat(found).isPresent();
        assertThat(found.get().getBizNo()).isEqualTo("123-45-67890");
    }

    @Test
    @DisplayName("사업자번호 존재 여부 확인")
    void existsByBizNo_Success() {
        CorpMast corp = CorpMast.builder()
                .sellerId("seller001")
                .bizNm("테스트법인")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        corpMastRepository.save(corp);

        assertThat(corpMastRepository.existsByBizNo("123-45-67890")).isTrue();
        assertThat(corpMastRepository.existsByBizNo("999-99-99999")).isFalse();
    }

    @Test
    @DisplayName("시/도와 구/군으로 법인 목록 조회")
    void findBySiNmAndSggNm_Success() {
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

        CorpMast corp2 = CorpMast.builder()
                .sellerId("seller002")
                .bizNm("법인2")
                .bizNo("123-45-67891")
                .corpRegNo("110111-1234568")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        corpMastRepository.save(corp1);
        corpMastRepository.save(corp2);

        List<CorpMast> found = corpMastRepository.findBySiNmAndSggNm("서울특별시", "강남구");
        assertThat(found).hasSize(2);
    }

    @Test
    @DisplayName("시/도와 구/군으로 페이징 조회")
    void findBySiNmAndSggNm_WithPaging() {
        for (int i = 0; i < 15; i++) {
            CorpMast corp = CorpMast.builder()
                    .sellerId("seller" + i)
                    .bizNm("법인" + i)
                    .bizNo("123-45-6789" + i)
                    .corpRegNo("110111-123456" + i)
                    .regionCd("11680")
                    .siNm("서울특별시")
                    .sggNm("강남구")
                    .username("testuser")
                    .build();
            corpMastRepository.save(corp);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<CorpMast> page = corpMastRepository.findBySiNmAndSggNm("서울특별시", "강남구", pageable);

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("사업자번호 목록으로 조회")
    void findBizNosByBizNoIn_Success() {
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

        CorpMast corp2 = CorpMast.builder()
                .sellerId("seller002")
                .bizNm("법인2")
                .bizNo("123-45-67891")
                .corpRegNo("110111-1234568")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        corpMastRepository.save(corp1);
        corpMastRepository.save(corp2);

        List<String> bizNos = corpMastRepository.findBizNosByBizNoIn(
                List.of("123-45-67890", "123-45-67891", "999-99-99999"));

        assertThat(bizNos).hasSize(2);
        assertThat(bizNos).contains("123-45-67890", "123-45-67891");
    }

    @Test
    @DisplayName("유효한 법인등록번호 개수 조회")
    void countValidCorpRegNo_Success() {
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

        CorpMast corp2 = CorpMast.builder()
                .sellerId("seller002")
                .bizNm("법인2")
                .bizNo("123-45-67891")
                .corpRegNo("")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        corpMastRepository.save(corp1);
        corpMastRepository.save(corp2);

        long count = corpMastRepository.countValidCorpRegNo();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("유효한 지역코드 개수 조회")
    void countValidRegionCd_Success() {
        CorpMast corp = CorpMast.builder()
                .sellerId("seller001")
                .bizNm("법인1")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("서울특별시")
                .sggNm("강남구")
                .username("testuser")
                .build();

        corpMastRepository.save(corp);

        long count = corpMastRepository.countValidRegionCd();
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}

