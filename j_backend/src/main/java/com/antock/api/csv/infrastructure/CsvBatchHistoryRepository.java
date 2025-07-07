package com.antock.api.csv.infrastructure;

import com.antock.api.csv.domain.CsvBatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CsvBatchHistoryRepository extends JpaRepository<CsvBatchHistory, Long> {}