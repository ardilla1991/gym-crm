package com.epam.training.gym.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Trainee profile. Name, username, password and status are in {@link User}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Trainee implements UserProfile {

    private Long id;

    /** Personal data: excluded from toString() so it does not end up in logs. */
    @ToString.Exclude
    private LocalDate dateOfBirth;

    /** Personal data: excluded from toString() so it does not end up in logs. */
    @ToString.Exclude
    private String address;

    @NotNull
    @Valid
    private User user;

    /** Filled only where the trainers are loaded too: the trainee's profile. */
    @ToString.Exclude
    @Builder.Default
    private Set<Trainer> trainers = new HashSet<>();
}