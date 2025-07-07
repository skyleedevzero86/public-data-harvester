package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMastHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaCorpMastHistoryStore implements CorpMastHistoryStore {
    private final CorpMastHistoryRepository repo;

    @Override
    public void save(CorpMastHistory history) {
        repo.save(history);
    }
}