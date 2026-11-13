package com.epam.training.gym.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class TrainingEntityMapperTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 5);
    private static final TrainingTypeEntity YOGA = new TrainingTypeEntity(2L, TrainingTypeName.YOGA);
    private static final TraineeEntity JOHN = TraineeEntity.builder().id(5L).user(user("John", "Smith")).build();
    private static final TrainerEntity ANNA =
            TrainerEntity.builder().id(1L).user(user("Anna", "Lee")).specialization(YOGA).build();

    @Test
    void toModel_copiesAllFields() {
        TrainingEntity entity = TrainingEntity.builder()
                .id(10L).trainee(JOHN).trainer(ANNA).trainingType(YOGA)
                .trainingName("Morning yoga").trainingDate(DATE).trainingDuration(60)
                .build();

        Training model = TrainingEntityMapper.toModel(entity);

        assertEquals(10L, model.getId());
        assertEquals("John.Smith", model.getTrainee().getUser().getUsername());
        assertEquals("Anna.Lee", model.getTrainer().getUser().getUsername());
        assertEquals(TrainingTypeName.YOGA, model.getTrainingType().getTrainingTypeName());
        assertEquals("Morning yoga", model.getTrainingName());
        assertEquals(DATE, model.getTrainingDate());
        assertEquals(60, model.getTrainingDuration());
    }

    @Test
    void toNewEntity_takesLoadedEntitiesAndModelData() {
        Training model = Training.builder()
                .id(99L).trainingName("Morning yoga").trainingDate(DATE).trainingDuration(60)
                .build();

        TrainingEntity entity = TrainingEntityMapper.toNewEntity(model, JOHN, ANNA, YOGA);

        assertNull(entity.getId());
        assertSame(JOHN, entity.getTrainee());
        assertSame(ANNA, entity.getTrainer());
        assertSame(YOGA, entity.getTrainingType());
        assertEquals("Morning yoga", entity.getTrainingName());
        assertEquals(DATE, entity.getTrainingDate());
        assertEquals(60, entity.getTrainingDuration());
    }

    private static UserEntity user(String firstName, String lastName) {
        return UserEntity.builder()
                .firstName(firstName).lastName(lastName).username(firstName + "." + lastName)
                .password("secret1234").isActive(true)
                .build();
    }
}
