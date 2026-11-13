package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.persistence.entity.TraineeEntity;

import java.util.Optional;

public interface TraineeDao {

    TraineeEntity save(TraineeEntity trainee);

    Optional<TraineeEntity> findByUsername(String username);

    /** Hard delete: the trainee's user, trainings and trainer assignments are deleted too. */
    void delete(TraineeEntity trainee);

    /** Like findByUsername, but also loads the trainee's trainers: for returning the profile. */
    Optional<TraineeEntity> findByUsernameWithTrainers(String username);
}