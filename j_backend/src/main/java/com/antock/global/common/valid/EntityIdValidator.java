package com.antock.global.common.valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EntityIdValidator {

    private final EntityClassRegistry entityClassRegistry;
    private final List<EntityIdValidationRule> validationRules;
    private final ValidationResultHandler resultHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void validateEntityIds() {
        log.info("엔티티 @Id 검증 시작");

        List<String> errors = new ArrayList<>();
        List<Class<?>> entityClasses = entityClassRegistry.getEntityClasses();

        for (Class<?> entityClass : entityClasses) {
            EntityIdValidationRule rule = findValidationRule(entityClass);
            if (rule != null) {
                errors.addAll(rule.validate(entityClass));
            }
        }

        resultHandler.handleValidationErrors(errors, entityClasses.size());
    }

    private EntityIdValidationRule findValidationRule(Class<?> entityClass) {
        return validationRules.stream()
                .filter(rule -> rule.supports(entityClass))
                .findFirst()
                .orElse(null);
    }
}
