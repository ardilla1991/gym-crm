package com.epam.training.gym.domain;

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
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    /** Duration in minutes. */
    private int trainingDuration;

}
