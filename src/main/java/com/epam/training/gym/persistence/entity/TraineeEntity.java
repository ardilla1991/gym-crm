package com.epam.training.gym.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Database row of a trainee (table "trainee"). Name, username, password and status are in {@link UserEntity}.
 */
@Entity(name = "Trainee")
@Table(name = "trainee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TraineeEntity implements ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Personal data: excluded from toString() so it does not end up in logs. */
    @ToString.Exclude
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** Personal data: excluded from toString() so it does not end up in logs. */
    @ToString.Exclude
    @Column(name = "address")
    private String address;

    /** Saved and deleted together with the trainee. */
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    /** Owning side of the many-to-many relation: rows of trainee_trainer are managed from here. */
    @ToString.Exclude
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"),
            indexes = @Index(name = "idx_trainee_trainer_trainer", columnList = "trainer_id"))
    private Set<TrainerEntity> trainers = new HashSet<>();

    /** Deleting a trainee also deletes the trainee's trainings. */
    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE)
    private List<TrainingEntity> trainings = new ArrayList<>();
}
