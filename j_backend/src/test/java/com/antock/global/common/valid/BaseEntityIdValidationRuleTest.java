package com.antock.global.common.valid;

import com.antock.global.common.entity.BaseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseEntityIdValidationRule 테스트")
class BaseEntityIdValidationRuleTest {

    private final BaseEntityIdValidationRule rule = new BaseEntityIdValidationRule();

    @Test
    @DisplayName("BaseEntity를 상속받는 클래스를 지원함")
    void supports() {
        assertThat(rule.supports(com.antock.api.member.domain.Member.class)).isTrue();
        assertThat(rule.supports(com.antock.api.health.domain.HealthCheck.class)).isTrue();
    }

    @Test
    @DisplayName("BaseEntity를 상속받지 않는 클래스는 지원하지 않음")
    void doesNotSupport() {
        assertThat(rule.supports(com.antock.api.csv.domain.CsvBatchHistory.class)).isFalse();
    }

    @Test
    @DisplayName("BaseEntity 상속 엔티티에 @Id가 없으면 검증 통과")
    void validate_noIdField() {
        List<String> errors = rule.validate(com.antock.api.member.domain.Member.class);
        assertThat(errors).isEmpty();
    }
}

