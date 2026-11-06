package com.epam.training.gym.service;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.service.exception.EntityNotFoundException;

import java.util.List;

public interface TraineeService {
    /**
     * Creates a trainee profile with a generated username and password.
     * @return the saved trainee; its user holds the generated username and password
     * @throws jakarta.validation.ConstraintViolationException if a required field is missing
     */
    Trainee create(Trainee trainee);

    /*
    Trainee update(Trainee trainee);
    void delete(Long id);
    /** @throws EntityNotFoundException if there is no trainee with this id */
    /*Trainee findById(Long id);
    List<Trainee> findAll();
    */
}
