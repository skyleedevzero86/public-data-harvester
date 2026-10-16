package com.antock.api.admin.health;

import com.antock.api.member.application.service.RateLimitServiceInterface;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisHealthIndicatorTest {

    @Test
    void publishesRecoveryEventOnceWhenStatusReturnsToUp() {
        RateLimitServiceInterface rateLimitService = mock(RateLimitServiceInterface.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        when(rateLimitService.probeRedis()).thenReturn(false, true, true);
        RedisHealthIndicator indicator = new RedisHealthIndicator(rateLimitService, publisher);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);

        verify(publisher, times(1)).publishEvent(any(RedisRecoveredEvent.class));
    }

    @Test
    void doesNotPublishWhenRedisStaysUp() {
        RateLimitServiceInterface rateLimitService = mock(RateLimitServiceInterface.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        when(rateLimitService.probeRedis()).thenReturn(true);
        RedisHealthIndicator indicator = new RedisHealthIndicator(rateLimitService, publisher);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);

        verify(publisher, never()).publishEvent(any());
    }
}
