package com.antock.api.coseller.application.service;

import com.antock.global.jpa.SqlStatementCounter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Nplus1MeasurementService {

    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Nplus1Probe.Report measure(int limit) {
        try {
            Nplus1Probe.Report report = Nplus1Probe.measure(entityManager, limit);
            log.info("N+1 측정: 법인 {}건, 건별 조회 {}회, @BatchSize({}) {}회",
                    report.rows(),
                    report.before().queries(),
                    report.batchSize(),
                    report.after().queries());
            return report;
        } finally {
            SqlStatementCounter.close();
        }
    }
}
