package com.epam.training.gym.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.User;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class TraineeEntityMapperTest {

    private static final LocalDate BIRTHDAY = LocalDate.of(1995, 4, 12);

    @Test
    void toModel_copiesUserAndPersonalData_withoutTrainers() {
        Trainee model = TraineeEntityMapper.toModel(traineeWithTrainer());

        assertEquals(5L, model.getId());
        assertEquals("John.Smith", model.getUser().getUsername());
        assertEquals(BIRTHDAY, model.getDateOfBirth());
        assertEquals("12 Oak Street", model.getAddress());
        assertTrue(model.getTrainers().isEmpty());       // not touched: no extra query
    }

    @Test
    void toModelWithTrainers_addsTrainersWithoutTheirTrainees() {
        Trainee model = TraineeEntityMapper.toModelWithTrainers(traineeWithTrainer());

        assertEquals(1, model.getTrainers().size());
        Trainer trainer = model.getTrainers().iterator().next();
        assertEquals("Anna.Lee", trainer.getUser().getUsername());
        assertTrue(trainer.getTrainees().isEmpty());     // one level deep
    }

    @Test
    void toNewEntity_copiesDataButNotId() {
        Trainee model = Trainee.builder()
                .id(99L)
                .user(User.builder().firstName("John").lastName("Smith").username("John.Smith")
                        .password("secret1234").isActive(true).build())
                .dateOfBirth(BIRTHDAY)
                .address("12 Oak Street")
                .build();

        TraineeEntity entity = TraineeEntityMapper.toNewEntity(model);

        assertNull(entity.getId());
        assertEquals("John.Smith", entity.getUser().getUsername());
        assertEquals(BIRTHDAY, entity.getDateOfBirth());
        assertEquals("12 Oak Street", entity.getAddress());
    }

    /** John (trainee) with one trainer, Anna, who in turn has John in her trainees. */
    private static TraineeEntity traineeWithTrainer() {
        TraineeEntity john = TraineeEntity.builder()
                .id(5L).user(user("John", "Smith")).dateOfBirth(BIRTHDAY).address("12 Oak Street")
                .build();
        TrainerEntity anna = TrainerEntity.builder()
                .id(1L).user(user("Anna", "Lee")).specialization(new TrainingTypeEntity(2L, TrainingTypeName.YOGA))
                .build();
        john.getTrainers().add(anna);
        anna.getTrainees().add(john);
        return john;
    }

    private static UserEntity user(String firstName, String lastName) {
        return UserEntity.builder()
                .firstName(firstName).lastName(lastName).username(firstName + "." + lastName)
                .password("secret1234").isActive(true)
                .build();
    }
}
