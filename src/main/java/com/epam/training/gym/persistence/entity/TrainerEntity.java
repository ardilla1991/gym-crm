package com.epam.training.gym.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Database row of a trainer (table "trainer"). Name, username, password and status are in {@link UserEntity}.
 */
@Entity(name = "Trainer")
@Table(name = "trainer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TrainerEntity implements ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingTypeEntity specialization;

    /** Saved together with the trainer. */
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    /** Inverse side of TraineeEntity.trainers: read-only view, changes are made through the trainee. */
    @ToString.Exclude
    @Builder.Default
    @ManyToMany(mappedBy = "trainers")
    private Set<TraineeEntity> trainees = new HashSet<>();
}
