package com.antock.api.health.infrastructure;

import com.antock.api.health.domain.HealthCheck;
import com.antock.api.health.domain.HealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("HealthCheckRepository 테스트")
class HealthCheckRepositoryTest {

    @Autowired
    private HealthCheckRepository healthCheckRepository;

    @Test
    @DisplayName("헬스 체크 저장 및 조회")
    void saveAndFind() {
        HealthCheck healthCheck = HealthCheck.builder()
                .checkType("scheduled")
                .component("database")
                .status(HealthStatus.UP)
                .message("정상")
                .responseTime(150L)
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        HealthCheck saved = healthCheckRepository.save(healthCheck);
        HealthCheck found = healthCheckRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getComponent()).isEqualTo("database");
        assertThat(found.getStatus()).isEqualTo(HealthStatus.UP);
    }

    @Test
    @DisplayName("컴포넌트별 최신 헬스 체크 조회")
    void findLatestByComponent() {
        HealthCheck oldCheck = HealthCheck.builder()
                .checkType("scheduled")
                .component("database")
                .status(HealthStatus.UP)
                .message("정상")
                .checkedAt(LocalDateTime.now().minusHours(1))
                .build();

        HealthCheck newCheck = HealthCheck.builder()
                .checkType("scheduled")
                .component("database")
                .status(HealthStatus.UP)
                .message("정상")
                .checkedAt(LocalDateTime.now())
                .build();

        healthCheckRepository.save(oldCheck);
        healthCheckRepository.save(newCheck);

        var latest = healthCheckRepository.findLatestByComponent("database");
        assertThat(latest).isPresent();
    }

    @Test
    @DisplayName("만료된 헬스 체크 조회")
    void findExpiredChecks() {
        HealthCheck expired = HealthCheck.builder()
                .checkType("scheduled")
                .component("database")
                .status(HealthStatus.UP)
                .message("정상")
                .checkedAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().minusMinutes(30))
                .build();

        HealthCheck valid = HealthCheck.builder()
                .checkType("scheduled")
                .component("redis")
                .status(HealthStatus.UP)
                .message("정상")
                .checkedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        healthCheckRepository.save(expired);
        healthCheckRepository.save(valid);

        List<HealthCheck> expiredChecks = healthCheckRepository.findExpiredChecks(LocalDateTime.now());
        assertThat(expiredChecks).isNotEmpty();
    }

    @Test
    @DisplayName("컴포넌트별 헬스 체크 페이징 조회")
    void findByComponent_WithPaging() {
        for (int i = 0; i < 15; i++) {
            HealthCheck check = HealthCheck.builder()
                    .checkType("scheduled")
                    .component("database")
                    .status(HealthStatus.UP)
                    .message("정상")
                    .checkedAt(LocalDateTime.now().minusMinutes(i))
                    .build();
            healthCheckRepository.save(check);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<HealthCheck> page = healthCheckRepository.findByComponentOrderByCheckedAtDesc("database", pageable);

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(15);
    }
}

