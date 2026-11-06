package com.epam.training.gym.domain;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Trainee profile (table "trainee"). Name, username, password and status are in {@link User}.
 */
@Entity
@Table(name = "trainee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Personal data: excluded from toString() so it does not end up in logs. */
    @Column(name = "date_of_birth")
    @ToString.Exclude
    private LocalDate dateOfBirth;

    /** Personal data: excluded from toString() so it does not end up in logs. */
    @ToString.Exclude
    @Column(name = "address")
    private String address;

    /** Saved and deleted together with the trainee. */
    @Valid
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Owning side of the many-to-many relation: rows of trainee_trainer are managed from here. */
    @ToString.Exclude
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id"))
    private Set<Trainer> trainers = new HashSet<>();

    /** Deleting a trainee also deletes the trainee's trainings. */
    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE)
    private List<Training> trainings = new ArrayList<>();
}
