package com.epam.training.gym.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import org.junit.jupiter.api.Test;

class TrainingTypeEntityMapperTest {

    @Test
    void toModel_copiesIdAndName() {
        TrainingType model = TrainingTypeEntityMapper.toModel(new TrainingTypeEntity(2L, TrainingTypeName.YOGA));

        assertEquals(2L, model.getId());
        assertEquals(TrainingTypeName.YOGA, model.getTrainingTypeName());
    }
}
