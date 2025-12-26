package com.antock.global.common.valid;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NonBaseEntityIdValidationRule 테스트")
class NonBaseEntityIdValidationRuleTest {

    private final NonBaseEntityIdValidationRule rule = new NonBaseEntityIdValidationRule();

    @Test
    @DisplayName("BaseEntity를 상속받지 않는 클래스를 지원함")
    void supports() {
        assertThat(rule.supports(com.antock.api.csv.domain.CsvBatchHistory.class)).isTrue();
    }

    @Test
    @DisplayName("BaseEntity를 상속받는 클래스는 지원하지 않음")
    void doesNotSupport() {
        assertThat(rule.supports(com.antock.api.member.domain.Member.class)).isFalse();
    }

    @Test
    @DisplayName("@Id가 있으면 검증 통과")
    void validate_hasIdField() {
        List<String> errors = rule.validate(com.antock.api.csv.domain.CsvBatchHistory.class);
        assertThat(errors).isEmpty();
    }
}
