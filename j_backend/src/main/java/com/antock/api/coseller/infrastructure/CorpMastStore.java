package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import java.util.List;
import java.util.Optional;

public interface CorpMastStore {
    void save(CorpMast corpMast);

    void saveAll(List<CorpMast> entityList);

    boolean existsByBizNo(String bizNo);

    Optional<CorpMast> findByBizNo(String bizNo);

    List<String> findExistingBizNos(List<String> bizNos);
}