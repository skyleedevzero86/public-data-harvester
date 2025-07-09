package com.antock.api.csv.infrastructure;

import com.antock.api.csv.domain.CsvBatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CsvBatchHistoryRepository extends JpaRepository<CsvBatchHistory, Long> {
    List<CsvBatchHistory> findTop10ByOrderByTimestampDesc();
}