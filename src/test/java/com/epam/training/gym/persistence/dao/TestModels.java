package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.User;

import java.time.LocalDate;

/** New models for the DAO save methods; the ids are left empty. */
public final class TestModels {

    private TestModels() {
    }

    public static User newUserModel(String firstName, String lastName, String username) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password("abcde12345")
                .isActive(true)
                .build();
    }

    public static Trainee newTraineeModel(String username) {
        return Trainee.builder()
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .address("1 Main Street")
                .user(newUserModel("John", "Smith", username))
                .build();
    }

    /** The specialization is an existing training type, referenced by its id. */
    public static Trainer newTrainerModel(String username, Long specializationId, TrainingTypeName specialization) {
        return Trainer.builder()
                .specialization(new TrainingType(specializationId, specialization))
                .user(newUserModel("Anna", "Lee", username))
                .build();
    }
}
