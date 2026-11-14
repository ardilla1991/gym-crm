package com.epam.training.gym.service.mapper;

import com.epam.training.gym.model.Training;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;

/** Converts a training between the model used by the services and the database entity. */
public final class TrainingEntityMapper {

    private TrainingEntityMapper() {
    }

    /** The trainee and the trainer come without their lists (see their mappers). */
    public static Training toModel(TrainingEntity entity) {
        return Training.builder()
                .id(entity.getId())
                .trainee(TraineeEntityMapper.toModel(entity.getTrainee()))
                .trainer(TrainerEntityMapper.toModel(entity.getTrainer()))
                .trainingName(entity.getTrainingName())
                .trainingType(TrainingTypeEntityMapper.toModel(entity.getTrainingType()))
                .trainingDate(entity.getTrainingDate())
                .trainingDuration(entity.getTrainingDuration())
                .build();
    }

    /**
     * A new row for a new training. The trainee, the trainer and the type are entities
     * loaded by the service; from the model only the name, date and duration are taken.
     */
    public static TrainingEntity toNewEntity(Training model, TraineeEntity trainee, TrainerEntity trainer,
                                             TrainingTypeEntity trainingType) {
        return TrainingEntity.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .trainingName(model.getTrainingName())
                .trainingDate(model.getTrainingDate())
                .trainingDuration(model.getTrainingDuration())
                .build();
    }
}
