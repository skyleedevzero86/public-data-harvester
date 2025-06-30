package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorpMastRepository extends JpaRepository<CorpMast, Long> {
    //저장시 사업자 번호 중복 확인
    boolean existsByBizNo(String bizNo);
}