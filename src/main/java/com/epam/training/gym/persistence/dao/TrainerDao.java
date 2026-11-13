package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.persistence.entity.TrainerEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrainerDao {

    TrainerEntity save(TrainerEntity trainer);

    Optional<TrainerEntity> findByUsername(String username);

    /** For updating a trainee's trainers list by trainer usernames. */
    List<TrainerEntity> findByUsernames(Collection<String> usernames);

    /** Trainers that are not in the trainee's trainers list. */
    List<TrainerEntity> findNotAssignedToTrainee(String traineeUsername);

    /** Like findByUsername, but also loads the trainer's trainees: for returning the profile. */
    Optional<TrainerEntity> findByUsernameWithTrainees(String username);
}