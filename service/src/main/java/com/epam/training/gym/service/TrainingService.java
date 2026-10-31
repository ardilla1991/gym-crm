package com.epam.training.gym.service;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.service.exception.EntityNotFoundException;

import java.util.List;

public interface TrainingService {

    Training create(Training training);
    /** @throws EntityNotFoundException if there is no training with this id */
    Training findById(Long id);
    List<Training> findAll();
}
