package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.User;

import java.time.LocalDate;

/** Builders of not yet saved entities for DAO tests. */
public class TestEntities {

    private TestEntities() {
    }

    public static User newUser(String firstName, String lastName, String username) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password("abcdefghij")
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
}
