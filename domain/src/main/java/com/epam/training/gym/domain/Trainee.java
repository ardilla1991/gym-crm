package com.epam.training.gym.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Trainee. Inherits name, username, password and status from {@link User}.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Trainee extends User {
    private Long id;
    /** Personal data: excluded from toString() so it does not end up in logs. */
    @ToString.Exclude
    private LocalDate dateOfBirth;
    /** Personal data: excluded from toString() so it does not end up in logs. */
    @ToString.Exclude
    private String address;
}
