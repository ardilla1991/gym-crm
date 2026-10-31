package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    /** Assigns a new id to the trainee and stores it. */
    Trainee save(Trainee trainee);
    /** Replaces the stored trainee with the same id. */
    Trainee update(Trainee trainee);
    void delete(Long id);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
}
