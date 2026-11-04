package com.epam.training.gym.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Common profile data of a trainee or a trainer (table "users").
 * "user" is a reserved word in PostgreSQL and H2, so the table is called "users".
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Unique among all users, so a trainee and a trainer can never share a username. */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** Excluded from toString() so it never ends up in logs. */
    @ToString.Exclude
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}
