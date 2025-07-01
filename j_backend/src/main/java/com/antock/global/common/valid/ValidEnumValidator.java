package com.antock.global.common.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidEnumValidator implements ConstraintValidator<ValidEnum, Enum<?>> {

    private Set<String> enumValues;
    private String message;

    @Override
    public void initialize(ValidEnum targetEnum) {
        Class<? extends Enum<?>> enumClass = targetEnum.target();
        enumValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
        message = targetEnum.message(); // message 저장
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {

        // 유효한 Enum 값인지 확인
        if (value == null || !enumValues.contains(value.name())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(this.message) // << 여기에 주목
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}