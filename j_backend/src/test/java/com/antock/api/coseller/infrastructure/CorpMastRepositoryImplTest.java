package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CorpMastRepositoryImpl 테스트")
class CorpMastRepositoryImplTest {

    @Autowired
    private CorpMastRepository corpMastRepository;

    @Test
    @DisplayName("지역 통계 조회 성공")
    void getRegionStats_Success() {
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
                .sggNm("서초구")
                .username("testuser")
                .build();

        corpMastRepository.save(corp1);
        corpMastRepository.save(corp2);

        List<Object[]> stats = corpMastRepository.getRegionStats();
        assertThat(stats).isNotEmpty();
    }

    @Test
    @DisplayName("런타임 DDL 실행은 지원되지 않음")
    void addMissingColumns_ThrowsException() {
        CorpMastRepository repository = corpMastRepository;
        
        assertThatThrownBy(() -> repository.addMissingColumns())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("런타임 DDL 실행은 지원되지 않습니다");
    }

    @Test
    @DisplayName("샘플 데이터 추가 - Flyway 마이그레이션으로 대체됨")
    void addSampleData_Note() {
        CorpMast corp = CorpMast.builder()
                .sellerId("seller001")
                .bizNm("법인1")
                .bizNo("123-45-67890")
                .corpRegNo("110111-1234567")
                .regionCd("11680")
                .siNm("울산광역시")
                .sggNm("중구")
                .username("testuser")
                .build();

        CorpMast saved = corpMastRepository.save(corp);
        assertThat(saved).isNotNull();
        
        CorpMast updated = corpMastRepository.findById(corp.getId()).orElse(null);
        assertThat(updated).isNotNull();
    }
}

