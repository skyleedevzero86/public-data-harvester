package com.antock.api.coseller.infrastructure;

import com.antock.api.coseller.domain.CorpMastHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CorpMastHistoryRepository extends JpaRepository<CorpMastHistory, Long> {

    @Query("SELECT COUNT(h) FROM CorpMastHistory h WHERE h.action = 'INSERT' AND h.result = 'SUCCESS' AND h.message = '정상 저장'")
    long countAutoCollectSuccess();

    @Query("SELECT COUNT(h) FROM CorpMastHistory h WHERE h.action = 'INSERT' AND h.message = '정상 저장'")
    long countAutoCollectTotal();

    List<CorpMastHistory> findTop10ByOrderByTimestampDesc();
}