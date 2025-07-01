package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CorpMastRepository extends JpaRepository<CorpMast, Long> {
    //저장시 사업자 번호 중복 확인
    boolean existsByBizNo(String bizNo);

    //사업자 번호로 데이터 확인
    Optional<CorpMast> findByBizNo(@Param("bizNo") String bizNo );
}