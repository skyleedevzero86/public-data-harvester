package com.antock.global.common.valid;

import com.antock.global.common.entity.BaseEntity;
import com.antock.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EntityIdValidator 테스트")
class EntityIdValidatorTest {

    @Mock
    private EntityClassRegistry entityClassRegistry;

    @Mock
    private ValidationResultHandler resultHandler;

    private EntityIdValidator validator;
    private List<EntityIdValidationRule> validationRules;

    @BeforeEach
    void setUp() {
        validationRules = Arrays.asList(
                new BaseEntityIdValidationRule(),
                new NonBaseEntityIdValidationRule()
        );
        validator = new EntityIdValidator(entityClassRegistry, validationRules, resultHandler);
    }

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

    @Test
    @DisplayName("엔티티 클래스 목록을 가져와서 검증 수행")
    void validateEntityIds() {
        List<Class<?>> entityClasses = Arrays.asList(
                com.antock.api.member.domain.Member.class,
                com.antock.api.health.domain.HealthCheck.class
        );

        when(entityClassRegistry.getEntityClasses()).thenReturn(entityClasses);

        validator.validateEntityIds();
    }
}

