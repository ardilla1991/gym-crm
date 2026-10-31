package com.epam.training.gym.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Common part of every gym user.
 * Abstract: a user always exists as either a {@link Trainee} or a {@link Trainer}.
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString
public abstract class User {
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    /** Excluded from toString() so it never ends up in logs. */
    @ToString.Exclude
    private String password;
    private boolean isActive;
}
