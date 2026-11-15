package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.Trainer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrainerDao {

    /** The specialization must be an existing training type: it is linked by its id. */
    Trainer save(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    /** For updating a trainee's trainers list by trainer usernames. */
    List<Trainer> findByUsernames(Collection<String> usernames);

    /** Trainers that are not in the trainee's trainers list. */
    List<Trainer> findNotAssignedToTrainee(String traineeUsername);

    /** Like findByUsername, but also loads the trainer's trainees: for returning the profile. */
    Optional<Trainer> findByUsernameWithTrainees(String username);

    /** Saves the first and last name; other fields are not changed. */
    Trainer update(Trainer trainer);
}
