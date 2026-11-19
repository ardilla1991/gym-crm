package com.epam.training.gym.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GymMetricsTest {

    private final MeterRegistry registry = new SimpleMeterRegistry();
    private final GymMetrics metrics = new GymMetrics(registry);

    @Test
    void registrations_areCountedByRole() {
        metrics.traineeRegistered();
        metrics.traineeRegistered();
        metrics.trainerRegistered();

        assertEquals(2, registry.get("gym.registrations").tag("role", "trainee").counter().count());
        assertEquals(1, registry.get("gym.registrations").tag("role", "trainer").counter().count());
    }

    @Test
    void trainingAdded_isCounted() {
        metrics.trainingAdded();

        assertEquals(1, registry.get("gym.trainings.added").counter().count());
    }

    @Test
    void authenticationFailed_isCounted() {
        metrics.authenticationFailed();

        assertEquals(1, registry.get("gym.authentication.failures").counter().count());
    }
}
