package com.antock.global.common.valid;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy =  {ValidEnumValidator.class})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    String message() default "잘못된 입력 입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    Class<? extends  Enum<?>> target();
}