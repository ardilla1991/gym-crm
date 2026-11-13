package com.epam.training.gym.service.mapper;

import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;

/** Converts a trainer between the model used by the services and the database entity. */
public final class TrainerEntityMapper {

    private TrainerEntityMapper() {
    }

    /**
     * User and specialization only. The trainees are not touched:
     * outside a JOIN FETCH query each access to them would be one more SQL query (N+1).
     */
    public static Trainer toModel(TrainerEntity entity) {
        return Trainer.builder()
                .id(entity.getId())
                .user(UserEntityMapper.toModel(entity.getUser()))
                .specialization(TrainingTypeEntityMapper.toModel(entity.getSpecialization()))
                .build();
    }

    /**
     * With the trainees, one level deep: the trainees come without their own trainers,
     * otherwise trainer -> trainee -> trainer -> ... would never end.
     * Only for an entity loaded together with its trainees (findByUsernameWithTrainees).
     */
    public static Trainer toModelWithTrainees(TrainerEntity entity) {
        Trainer trainer = toModel(entity);
        entity.getTrainees().forEach(trainee -> trainer.getTrainees().add(TraineeEntityMapper.toModel(trainee)));
        return trainer;
    }

    /**
     * A new row for a new trainer. The specialization is the entity loaded by the service:
     * training types are never created from the model.
     */
    public static TrainerEntity toNewEntity(Trainer model, TrainingTypeEntity specialization) {
        return TrainerEntity.builder()
                .user(UserEntityMapper.toNewEntity(model.getUser()))
                .specialization(specialization)
                .build();
    }
}
