package com.epam.training.gym.service.mapper;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.persistence.entity.TraineeEntity;

/** Converts a trainee between the model used by the services and the database entity. */
public final class TraineeEntityMapper {

    private TraineeEntityMapper() {
    }

    /**
     * User and personal data only. The trainers are not touched:
     * outside a JOIN FETCH query each access to them would be one more SQL query (N+1).
     */
    public static Trainee toModel(TraineeEntity entity) {
        return Trainee.builder()
                .id(entity.getId())
                .user(UserEntityMapper.toModel(entity.getUser()))
                .dateOfBirth(entity.getDateOfBirth())
                .address(entity.getAddress())
                .build();
    }

    /**
     * With the trainers, one level deep: the trainers come without their own trainees,
     * otherwise trainee -> trainer -> trainee -> ... would never end.
     * Only for an entity loaded together with its trainers (findByUsernameWithTrainers).
     */
    public static Trainee toModelWithTrainers(TraineeEntity entity) {
        Trainee trainee = toModel(entity);
        entity.getTrainers().forEach(trainer -> trainee.getTrainers().add(TrainerEntityMapper.toModel(trainer)));
        return trainee;
    }

    /** A new row for a new trainee: the ids are left empty, the database generates them. */
    public static TraineeEntity toNewEntity(Trainee model) {
        return TraineeEntity.builder()
                .user(UserEntityMapper.toNewEntity(model.getUser()))
                .dateOfBirth(model.getDateOfBirth())
                .address(model.getAddress())
                .build();
    }
}
