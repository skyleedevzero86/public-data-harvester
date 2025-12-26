package com.antock.global.common.valid;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EntityClassRegistry {

    private static final Class<?>[] ENTITY_CLASSES = {
            com.antock.api.member.domain.Member.class,
            com.antock.api.health.domain.HealthCheck.class,
            com.antock.api.health.domain.SystemHealth.class,
            com.antock.api.coseller.domain.CorpMast.class,
            com.antock.api.member.domain.PasswordResetToken.class,
            com.antock.api.coseller.domain.CorpMastHistory.class,
            com.antock.api.file.domain.File.class,
            com.antock.api.member.domain.MemberPasswordHistory.class,
            com.antock.api.csv.domain.CsvBatchHistory.class
    };

    public List<Class<?>> getEntityClasses() {
        return Arrays.asList(ENTITY_CLASSES);
    }
}

