package com.antock.global.common.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidEnumValidator implements ConstraintValidator<ValidEnum, Enum<?>> {

    private Set<String> enumValues;

    @Override
    public void initialize(ValidEnum targetEnum) {
        Class<? extends Enum<?>> enumClass = targetEnum.target();
        enumValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if(value == null) {
            return false;
        }
        return enumValues.contains(value.name());
    }
}