package com.antock.global.common.valid;

import java.util.List;

public interface EntityIdValidationRule {
    List<String> validate(Class<?> entityClass);
    boolean supports(Class<?> entityClass);
}

