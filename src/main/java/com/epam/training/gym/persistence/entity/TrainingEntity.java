package com.epam.training.gym.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Database row of a training (table "training"): one trainee, one trainer, one training type.
 */
@Entity(name = "Training")
@Table(name = "training", indexes = {
        @Index(name = "idx_training_trainee_date", columnList = "trainee_id, training_date"),
        @Index(name = "idx_training_trainer_date", columnList = "trainer_id, training_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TrainingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeEntity trainee;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    @Column(name = "training_name", nullable = false)
    private String trainingName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingTypeEntity trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    /** Duration in minutes. */
    @Column(name = "training_duration", nullable = false)
    private int trainingDuration;
}
