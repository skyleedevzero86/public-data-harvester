package com.antock.global.common.valid;

import com.antock.global.common.entity.BaseEntity;
import jakarta.persistence.Id;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class NonBaseEntityIdValidationRule implements EntityIdValidationRule {

    @Override
    public boolean supports(Class<?> entityClass) {
        return !BaseEntity.class.isAssignableFrom(entityClass);
    }

    @Override
    public List<String> validate(Class<?> entityClass) {
        List<String> errors = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();
        boolean hasIdField = Stream.of(fields)
                .anyMatch(field -> field.isAnnotationPresent(Id.class));

        if (!hasIdField) {
            errors.add(String.format("%s: @Entity인데 @Id가 없습니다.", entityClass.getSimpleName()));
        }

        return errors;
    }
}

