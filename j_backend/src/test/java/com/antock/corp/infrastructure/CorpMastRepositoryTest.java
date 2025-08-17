package com.antock.corp.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.infrastructure.CorpMastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@EntityScan(basePackages = "com.antock.enterprise_info_crawler.api.coseller.domain")
@EnableJpaRepositories(basePackages = "com.antock.enterprise_info_crawler.api.coseller.infrastructure")

public class CorpMastRepositoryTest {

    @Autowired
    private CorpMastRepository corpMastRepository;

    //엔티티
    CorpMast entity ;
    @BeforeEach
    void setUp() {
        entity = CorpMast.builder()
                .sellerId("2025-서울강남-00789")
                .bizNo("518-42-01193")
                .bizNm("신시어랩")
                .corpRegNo("111111-1234567")
                .regionCd("1168010300")
                .build();
    }

    @Test
    @DisplayName("엔티티 저장 및 사업자 등록번호로 조회")
    public void save_find_by_bizNo() throws Exception {
        //given

        //when
        corpMastRepository.save(entity);

        //then
        Optional<CorpMast> result = corpMastRepository.findByBizNo(entity.getBizNo());
        assertThat(result).isPresent();
        assertThat(result.get().getBizNm()).isEqualTo(entity.getBizNm());
    }


    @Test
    @DisplayName("중복된 bizNo 저장 시 예외 발생 검증")
    void exception_duplicate_bizNo() {
        // given
        CorpMast duplicatedBizNoEntity = CorpMast.builder()
                .sellerId("test")
                .bizNm("테스트")
                .bizNo("518-42-01193")
                .build();

        corpMastRepository.save(entity);
        corpMastRepository.flush(); // 강제 insert

        // when & then
        assertThatThrownBy(() -> {
            corpMastRepository.saveAndFlush(duplicatedBizNoEntity);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("bizNo 존재 확인 메서드")
    void existsByBizNo_test() {
        // given
        corpMastRepository.save(entity);

        // when
        boolean exists = corpMastRepository.existsByBizNo("518-42-01193");

        // then
        assertThat(exists).isTrue();
    }
}
