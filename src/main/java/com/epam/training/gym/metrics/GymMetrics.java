package com.epam.training.gym.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsAdded;
    private final Counter authenticationFailures;

    public GymMetrics(MeterRegistry registry) {
        traineeRegistrations = counter(registry, "gym.registrations", "Registered trainees and trainers", "role", "trainee");
        trainerRegistrations = counter(registry, "gym.registrations", "Registered trainees and trainers", "role", "trainer");
        trainingsAdded = counter(registry, "gym.trainings.added", "Trainings added");
        authenticationFailures = counter(registry, "gym.authentication.failures",
                "REST calls with a wrong username or password");
    }

    public void traineeRegistered() {
        traineeRegistrations.increment();
    }

    public void trainerRegistered() {
        trainerRegistrations.increment();
    }

    public void trainingAdded() {
        trainingsAdded.increment();
    }

    public void authenticationFailed() {
        authenticationFailures.increment();
    }

    private static Counter counter(MeterRegistry registry, String name, String description, String... tags) {
        return Counter.builder(name).description(description).tags(tags).register(registry);
    }
}
