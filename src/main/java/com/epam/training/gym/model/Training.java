package com.epam.training.gym.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

/**
 * A training session: one trainee, one trainer, one training type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Training {

    private Long id;

    @NotNull
    @ToString.Exclude
    private Trainee trainee;

    @NotNull
    @ToString.Exclude
    private Trainer trainer;

    @NotBlank
    private String trainingName;

    @NotNull
    private TrainingType trainingType;

    @NotNull
    private LocalDate trainingDate;

    /** Duration in minutes. */
    @Positive
    private int trainingDuration;
}