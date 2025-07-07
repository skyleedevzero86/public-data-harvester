package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMastHistory;

public interface CorpMastHistoryStore {
    void save(CorpMastHistory history);
}