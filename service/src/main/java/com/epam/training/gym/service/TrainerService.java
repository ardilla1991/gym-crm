package com.epam.training.gym.service;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.service.exception.EntityNotFoundException;

import java.util.List;

public interface TrainerService extends AccountService {
    /**
     * Creates a trainer profile with a generated username and password.
     * Only the name of the specialization is used: the type itself is taken from the database.
     * @throws jakarta.validation.ConstraintViolationException if a required field is missing
     * @throws com.epam.training.gym.service.exception.EntityNotFoundException if the training type does not exist
     */
    Trainer create(Trainer trainer);

   // Trainer update(Trainer trainer);
    /** @throws EntityNotFoundException if there is no trainer with this id */
    /*Trainer findById(Long id);
    List<Trainer> findAll();*/
}
