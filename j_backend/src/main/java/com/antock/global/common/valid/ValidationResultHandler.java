package com.antock.global.common.valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ValidationResultHandler {

    public void handleValidationErrors(List<String> errors, int totalValidated) {
        if (!errors.isEmpty()) {
            log.error("엔티티 @Id 검증 실패:");
            errors.forEach(log::error);
            throw new IllegalStateException("엔티티 @Id 검증 실패: " + String.join(", ", errors));
        }

        log.info("엔티티 @Id 검증 완료 - 총 {}개 엔티티 검증", totalValidated);
    }
}

