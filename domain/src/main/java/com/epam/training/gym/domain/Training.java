package com.epam.training.gym.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

/**
 * A training session (table "training"): one trainee, one trainer, one training type.
 */
@Entity
@Table(name = "training")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @NotNull
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @NotBlank
    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingType trainingType;

    @NotNull
    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    /** Duration in minutes. */
    @NotNull
    @Positive
    @Column(name = "training_duration", nullable = false)
    private int trainingDuration;

}
