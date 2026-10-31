package com.epam.training.gym.service;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.service.exception.EntityNotFoundException;

import java.util.List;

public interface TrainerService {
    /** Calculates username, password and userId,
     * then stores the trainer.
     * */
    Trainer create(Trainer trainer);
    Trainer update(Trainer trainer);
    /** @throws EntityNotFoundException if there is no trainer with this id */
    Trainer findById(Long id);
    List<Trainer> findAll();
}
