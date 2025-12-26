package com.antock.global.common.valid;

import com.antock.global.common.entity.BaseEntity;
import jakarta.persistence.Id;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class EntityIdValidator {

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

    @EventListener(ApplicationReadyEvent.class)
    public void validateEntityIds() {
        log.info("엔티티 @Id 검증 시작");

        List<String> errors = new ArrayList<>();

        for (Class<?> entityClass : ENTITY_CLASSES) {
            if (BaseEntity.class.isAssignableFrom(entityClass)) {
                validateBaseEntitySubclass(entityClass, errors);
            } else {
                validateNonBaseEntity(entityClass, errors);
            }
        }

        if (!errors.isEmpty()) {
            log.error("엔티티 @Id 검증 실패:");
            errors.forEach(log::error);
            throw new IllegalStateException("엔티티 @Id 검증 실패: " + String.join(", ", errors));
        }

        log.info("엔티티 @Id 검증 완료 - 총 {}개 엔티티 검증", ENTITY_CLASSES.length);
    }

    private void validateBaseEntitySubclass(Class<?> entityClass, List<String> errors) {
        Field[] fields = entityClass.getDeclaredFields();
        boolean hasIdField = Stream.of(fields)
                .anyMatch(field -> field.isAnnotationPresent(Id.class));

        if (hasIdField) {
            errors.add(String.format("%s: BaseEntity를 상속받는데 @Id를 중복 정의했습니다. BaseEntity의 @Id를 사용해야 합니다.",
                    entityClass.getSimpleName()));
        }
    }

    private void validateNonBaseEntity(Class<?> entityClass, List<String> errors) {
        Field[] fields = entityClass.getDeclaredFields();
        boolean hasIdField = Stream.of(fields)
                .anyMatch(field -> field.isAnnotationPresent(Id.class));

        if (!hasIdField) {
            errors.add(String.format("%s: @Entity인데 @Id가 없습니다.", entityClass.getSimpleName()));
        }
    }
}
