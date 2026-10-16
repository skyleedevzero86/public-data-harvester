package com.antock.api.coseller.application.service;

import com.antock.api.coseller.domain.CorpMast;
import com.antock.api.coseller.domain.CorpMastHistory;
import com.antock.global.jpa.SqlStatementCounter;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

class Nplus1ProbeTest {

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void open() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:h2:mem:nplus1;DB_CLOSE_DELAY=-1")
                .applySetting(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.h2.Driver")
                .applySetting(AvailableSettings.JAKARTA_JDBC_USER, "sa")
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                .applySetting(AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .applySetting(AvailableSettings.STATEMENT_INSPECTOR, SqlStatementCounter.class.getName())
                .applySetting(AvailableSettings.SHOW_SQL, "false")
                .build();
        sessionFactory = new MetadataSources(registry)
                .addAnnotatedClass(CorpMast.class)
                .addAnnotatedClass(CorpMastHistory.class)
                .buildMetadata()
                .buildSessionFactory();
    }

    @AfterAll
    static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    void twoThousandRowsDropFromIndividualSelectsToBatches() {
        Nplus1Probe.Report report = measure(2000);
        System.out.println("N+1 2000 before=" + report.before().queries()
                + " after=" + report.after().queries()
                + " batch=" + report.batchSize());
        assertThat(report.rows()).isEqualTo(2000);
        assertThat(report.before().queries()).isEqualTo(2001);
        assertThat(report.after().queries()).isEqualTo(21);
        assertThat(report.before().historyRows()).isEqualTo(report.after().historyRows());
    }

    @Test
    void nineHundredRowsMatchTheCurrentDatabaseSize() {
        Nplus1Probe.Report report = measure(900);
        System.out.println("N+1 900 before=" + report.before().queries()
                + " after=" + report.after().queries());
        assertThat(report.rows()).isEqualTo(900);
        assertThat(report.before().queries()).isEqualTo(901);
        assertThat(report.after().queries()).isEqualTo(10);
    }

    private Nplus1Probe.Report measure(int rows) {
        EntityManager em = sessionFactory.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("delete from CorpMastHistory").executeUpdate();
        em.createQuery("delete from CorpMast").executeUpdate();
        em.getTransaction().commit();
        em.clear();

        em.getTransaction().begin();
        for (int i = 1; i <= rows; i++) {
            CorpMast corp = CorpMast.builder()
                    .sellerId("seller")
                    .bizNm("법인" + i)
                    .bizNo(String.format("b%05d", i))
                    .corpRegNo("1101111234567")
                    .regionCd("11680")
                    .siNm("서울특별시")
                    .sggNm("강남구")
                    .username("user")
                    .build();
            em.persist(corp);
            em.persist(CorpMastHistory.builder()
                    .username("user")
                    .action("INSERT")
                    .bizNo(corp.getBizNo())
                    .result("SUCCESS")
                    .corpMast(corp)
                    .build());
            if (i % 200 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.getTransaction().commit();
        em.clear();

        em.getTransaction().begin();
        Nplus1Probe.Report report = Nplus1Probe.measure(em, rows);
        em.getTransaction().commit();
        em.close();
        SqlStatementCounter.close();
        return report;
    }
}
