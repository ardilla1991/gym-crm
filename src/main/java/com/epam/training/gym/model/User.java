package com.epam.training.gym.model;

import com.epam.training.gym.model.validation.PersonName;
import lombok.*;

/** Common data of a trainee or a trainer: name, username, password and status. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

    private Long id;

    @PersonName
    private String firstName;

    @PersonName
    private String lastName;

    /** Unique among all users, so a trainee and a trainer can never share a username. */
    private String username;

    /** Excluded from toString() so it never ends up in logs. */
    @ToString.Exclude
    private String password;

    private boolean isActive;
}