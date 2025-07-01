package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;

import java.util.List;
import java.util.Optional;

/**
 * 저장소 확장성을 고려해 추상화 인터페이스 생성
 */
public interface CorpMastStore {
    void save(CorpMast corpMast);
    void saveAll(List<CorpMast> entityList);
    boolean existsByBizNo(String bizNo);
    Optional<CorpMast> findByBizNo(String bizNo);
}