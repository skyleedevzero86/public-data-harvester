package com.antock.api.coseller.application.service;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.domain.CorpMastHistory;
import org.hibernate.annotations.BatchSize;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;

public final class Nplus1Probe {

    private Nplus1Probe() {
    }

    public record Phase(int rows, int queries, long millis, int historyRows) {
    }

    public record Report(int rows, int batchSize, Phase before, Phase after) {
    }

    public static int batchSize() {
        try {
            Field histories = CorpMast.class.getDeclaredField("histories");
            BatchSize batchSize = histories.getAnnotation(BatchSize.class);
            return batchSize == null ? 0 : batchSize.size();
        } catch (NoSuchFieldException e) {
            return 0;
        }
    }

    public static Report measure(EntityManager em, int limit) {
        int available = em.createQuery("select count(c) from CorpMast c", Long.class)
                .getSingleResult()
                .intValue();
        int rows = Math.min(Math.max(limit, 0), available);
        if (rows == 0) {
            return new Report(0, batchSize(), new Phase(0, 0, 0, 0), new Phase(0, 0, 0, 0));
        }

        SqlCount.reset();
        long beforeStart = System.nanoTime();
        int beforeHistories = loadOneByOne(em, rows);
        Phase before = new Phase(rows, SqlCount.count(), elapsedMillis(beforeStart), beforeHistories);

        em.clear();
        SqlCount.reset();
        long afterStart = System.nanoTime();
        int afterHistories = loadWithMapping(em, rows);
        Phase after = new Phase(rows, SqlCount.count(), elapsedMillis(afterStart), afterHistories);
        em.clear();
        return new Report(rows, batchSize(), before, after);
    }

    private static int loadOneByOne(EntityManager em, int rows) {
        var ids = em.createQuery("select c.id from CorpMast c order by c.id", Long.class)
                .setMaxResults(rows)
                .getResultList();
        int historyRows = 0;
        for (Long id : ids) {
            historyRows += em.createQuery(
                    "select h from CorpMastHistory h where h.corpMast.id = :id", CorpMastHistory.class)
                    .setParameter("id", id)
                    .getResultList()
                    .size();
        }
        return historyRows;
    }

    private static int loadWithMapping(EntityManager em, int rows) {
        var corps = em.createQuery("select c from CorpMast c order by c.id", CorpMast.class)
                .setMaxResults(rows)
                .getResultList();
        int historyRows = 0;
        for (CorpMast corp : corps) {
            historyRows += corp.getHistories().size();
        }
        return historyRows;
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    public interface SqlCount {
        static void reset() {
            com.antock.global.jpa.SqlStatementCounter.reset();
        }

        static int count() {
            return com.antock.global.jpa.SqlStatementCounter.count();
        }
    }
}
