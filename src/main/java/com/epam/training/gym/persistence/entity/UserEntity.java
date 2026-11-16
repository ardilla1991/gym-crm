package com.epam.training.gym.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Database row of a user (table "users"): common data of a trainee or a trainer.
 * "user" is a reserved word in PostgreSQL and H2, so the table is called "users".
 * The entity name stays "User", so JPQL queries refer to it as before.
 */
@Entity(name = "User")
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserEntity {

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
