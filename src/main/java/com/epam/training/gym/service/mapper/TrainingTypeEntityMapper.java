package com.epam.training.gym.service.mapper;

import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;

/**
 * Converts a training type from the database entity to the model.
 * There is no conversion back: types are a fixed list, services always load them by name.
 */
public final class TrainingTypeEntityMapper {

    private TrainingTypeEntityMapper() {
    }

    public static TrainingType toModel(TrainingTypeEntity entity) {
        return new TrainingType(entity.getId(), entity.getTrainingTypeName());
    }
}
