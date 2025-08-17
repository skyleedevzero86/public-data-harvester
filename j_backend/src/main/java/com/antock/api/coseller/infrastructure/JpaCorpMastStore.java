package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMast;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaCorpMastStore implements CorpMastStore {

    private final CorpMastRepository corpMastRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(CorpMast corpMast) {
        corpMastRepository.save(corpMast);
    }

    @Override
    public void saveAll(List<CorpMast> entityList) {
        corpMastRepository.saveAll(entityList);
    }

    @Override
    public boolean existsByBizNo(String bizNo) {
        return corpMastRepository.existsByBizNo(bizNo);
    }

    @Override
    public Optional<CorpMast> findByBizNo(String bizNo) {
        return corpMastRepository.findByBizNo(bizNo);
    }

    @Override
    public List<String> findExistingBizNos(List<String> bizNos) {
        return corpMastRepository.findBizNosByBizNoIn(bizNos);
    }
}