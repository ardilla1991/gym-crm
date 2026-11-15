package com.epam.training.gym.model;

import com.epam.training.gym.model.validation.FieldLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Size(max = FieldLimits.TRAINING_NAME_MAX)
    private String trainingName;

    @NotNull
    private TrainingType trainingType;

    @NotNull
    private LocalDate trainingDate;

    /** Duration in minutes. */
    @Positive
    private int trainingDuration;
}