package com.antock.global.common.valid;

import com.antock.global.common.entity.BaseEntity;
import com.antock.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EntityIdValidator 테스트")
class EntityIdValidatorTest {

    @Test
    @DisplayName("BaseEntity에 @Id가 있는지 확인")
    void baseEntityHasId() {
        Field[] fields = BaseEntity.class.getDeclaredFields();
        boolean hasId = Stream.of(fields)
                .anyMatch(field -> field.isAnnotationPresent(Id.class));

        assertThat(hasId).isTrue();
    }

    @Test
    @DisplayName("BaseTimeEntity는 BaseEntity를 상속받아 @Id를 상속받음")
    void baseTimeEntityInheritsId() {
        assertThat(BaseTimeEntity.class.getSuperclass()).isEqualTo(BaseEntity.class);
        
        Field[] fields = BaseTimeEntity.class.getDeclaredFields();
        boolean hasId = Stream.of(fields)
                .anyMatch(field -> field.isAnnotationPresent(Id.class));

        assertThat(hasId).isFalse();
    }

    @Test
    @DisplayName("BaseEntity를 상속받는 엔티티는 @Id를 중복 정의하면 안됨")
    void entityExtendingBaseEntityShouldNotHaveDuplicateId() {
        Class<?>[] entityClasses = {
                com.antock.api.member.domain.Member.class,
                com.antock.api.health.domain.HealthCheck.class,
                com.antock.api.health.domain.SystemHealth.class,
                com.antock.api.coseller.domain.CorpMast.class,
                com.antock.api.member.domain.PasswordResetToken.class
        };

        for (Class<?> entityClass : entityClasses) {
            if (BaseEntity.class.isAssignableFrom(entityClass)) {
                Field[] fields = entityClass.getDeclaredFields();
                boolean hasId = Stream.of(fields)
                        .anyMatch(field -> field.isAnnotationPresent(Id.class));

                assertThat(hasId)
                        .as("%s는 BaseEntity를 상속받으므로 @Id를 중복 정의하면 안됩니다", entityClass.getSimpleName())
                        .isFalse();
            }
        }
    }
}

