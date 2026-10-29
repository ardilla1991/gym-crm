package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);
    Trainee update(Trainee trainee);
    void delete(Long id);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
}
