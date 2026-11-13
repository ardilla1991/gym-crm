package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.persistence.entity.UserEntity;

import java.time.LocalDate;

/** Builders of not yet saved entities for DAO tests. */
public final class TestEntities {

    private TestEntities() {
    }

    public static UserEntity newUser(String firstName, String lastName, String username) {
        return UserEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password("abcde12345")
                .isActive(true)
                .build();
    }

    public static TraineeEntity newTrainee(String username) {
        return TraineeEntity.builder()
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .address("1 Main Street")
                .user(newUser("John", "Smith", username))
                .build();
    }

    public static TrainerEntity newTrainer(String username, TrainingTypeEntity specialization) {
        return TrainerEntity.builder()
                .specialization(specialization)
                .user(newUser("Anna", "Lee", username))
                .build();
    }

    public static TraineeEntity newTrainee(String firstName, String lastName, String username) {
        return TraineeEntity.builder()
                .user(newUser(firstName, lastName, username))
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .address("1 Main Street")
                .build();
    }

    public static TrainerEntity newTrainer(String firstName, String lastName, String username,
                                     TrainingTypeEntity specialization) {
        return TrainerEntity.builder()
                .user(newUser(firstName, lastName, username))
                .specialization(specialization)
                .build();
    }
}
