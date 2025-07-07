package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMastHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorpMastHistoryRepository extends JpaRepository<CorpMastHistory, Long> {
}