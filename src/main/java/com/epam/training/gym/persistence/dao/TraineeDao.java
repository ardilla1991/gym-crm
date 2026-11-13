package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {

    Trainee save(Trainee trainee);

    Optional<Trainee> findByUsername(String username);

    /** Hard delete: the trainee's user, trainings and trainer assignments are deleted too. */
    void delete(Trainee trainee);

    /** Like findByUsername, but also loads the trainee's trainers: for returning the profile. */
    Optional<Trainee> findByUsernameWithTrainers(String username);
}
