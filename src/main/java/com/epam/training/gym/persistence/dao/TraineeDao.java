package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;

import java.util.Collection;
import java.util.Optional;

public interface TraineeDao {

    Trainee save(Trainee trainee);

    Optional<Trainee> findByUsername(String username);

    /** Like findByUsername, but also loads the trainee's trainers: for returning the profile. */
    Optional<Trainee> findByUsernameWithTrainers(String username);

    /** Saves the first and last name, date of birth and address; other fields are not changed. */
    Trainee update(Trainee trainee);

    /** Hard delete: the trainee's user, trainings and trainer assignments are deleted too. */
    void delete(Trainee trainee);

    /** The trainee's trainers list becomes exactly these trainers. */
    void replaceTrainers(Trainee trainee, Collection<Trainer> trainers);

    /** Adds the trainer to the trainee's trainers list, if it is not there yet. */
    void addTrainer(Trainee trainee, Trainer trainer);
}
