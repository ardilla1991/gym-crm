package com.epam.training.gym.service;

import com.epam.training.gym.domain.Trainee;

import java.util.List;

public interface TraineeService {
    /** Calculates username, password and userId,
     * then stores the trainee. */
    Trainee create(Trainee trainee);
    Trainee update(Trainee trainee);
    void delete(Long id);
    /** @throws java.util.NoSuchElementException if there is no trainee with this id */
    Trainee findById(Long id);
    List<Trainee> findAll();
}
