package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.User;

import java.time.LocalDate;

/** Builders of not yet saved entities for DAO tests. */
public final class TestEntities {

    private TestEntities() {
    }

    public static User newUser(String firstName, String lastName, String username) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password("abcde12345")
                .isActive(true)
                .build();
    }

    public static Trainee newTrainee(String username) {
        return Trainee.builder()
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .address("1 Main Street")
                .user(newUser("John", "Smith", username))
                .build();
    }

    public static Trainer newTrainer(String username, TrainingType specialization) {
        return Trainer.builder()
                .specialization(specialization)
                .user(newUser("Anna", "Lee", username))
                .build();
    }

    public static Trainee newTrainee(String firstName, String lastName, String username) {
        return Trainee.builder()
                .user(newUser(firstName, lastName, username))
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .address("1 Main Street")
                .build();
    }

    public static Trainer newTrainer(String firstName, String lastName, String username,
                                     TrainingType specialization) {
        return Trainer.builder()
                .user(newUser(firstName, lastName, username))
                .specialization(specialization)
                .build();
    }
}
