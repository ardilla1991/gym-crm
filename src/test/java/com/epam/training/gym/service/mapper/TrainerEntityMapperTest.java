package com.epam.training.gym.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.User;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

class TrainerEntityMapperTest {

    private static final TrainingTypeEntity YOGA = new TrainingTypeEntity(2L, TrainingTypeName.YOGA);

    @Test
    void toModel_copiesUserAndSpecialization_withoutTrainees() {
        TrainerEntity anna = trainerWithTrainee();

        Trainer model = TrainerEntityMapper.toModel(anna);

        assertEquals(1L, model.getId());
        assertEquals("Anna.Lee", model.getUser().getUsername());
        assertEquals(TrainingTypeName.YOGA, model.getSpecialization().getTrainingTypeName());
        assertTrue(model.getTrainees().isEmpty());       // not touched: no extra query
    }

    @Test
    void toModelWithTrainees_addsTraineesWithoutTheirTrainers() {
        Trainer model = TrainerEntityMapper.toModelWithTrainees(trainerWithTrainee());

        assertEquals(1, model.getTrainees().size());
        Trainee trainee = model.getTrainees().iterator().next();
        assertEquals("John.Smith", trainee.getUser().getUsername());
        assertTrue(trainee.getTrainers().isEmpty());     // one level deep
    }

    @Test
    void toNewEntity_usesGivenSpecializationAndNoId() {
        Trainer model = Trainer.builder()
                .id(99L)
                .user(User.builder().firstName("Anna").lastName("Lee").username("Anna.Lee")
                        .password("secret1234").isActive(true).build())
                .specialization(new TrainingType(null, TrainingTypeName.YOGA))
                .build();

        TrainerEntity entity = TrainerEntityMapper.toNewEntity(model, YOGA);

        assertNull(entity.getId());
        assertEquals("Anna.Lee", entity.getUser().getUsername());
        assertSame(YOGA, entity.getSpecialization());    // the loaded type, not a new one
    }

    /** Anna (trainer) with one trainee, John, who in turn has Anna in his trainers. */
    private static TrainerEntity trainerWithTrainee() {
        TrainerEntity anna = TrainerEntity.builder().id(1L).user(user("Anna", "Lee")).specialization(YOGA).build();
        TraineeEntity john = TraineeEntity.builder().id(5L).user(user("John", "Smith")).build();
        anna.getTrainees().add(john);
        john.getTrainers().add(anna);
        return anna;
    }

    private static UserEntity user(String firstName, String lastName) {
        return UserEntity.builder()
                .firstName(firstName).lastName(lastName).username(firstName + "." + lastName)
                .password("secret1234").isActive(true)
                .build();
    }
}
