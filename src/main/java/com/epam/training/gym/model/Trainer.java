package com.epam.training.gym.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Trainer profile. Name, username, password and status are in {@link User}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Trainer implements UserProfile {

    private Long id;

    @NotNull
    private TrainingType specialization;

    @NotNull
    @Valid
    private User user;

    /** Filled only where the trainees are loaded too: the trainer's profile. */
    @ToString.Exclude
    @Builder.Default
    private Set<Trainee> trainees = new HashSet<>();
}