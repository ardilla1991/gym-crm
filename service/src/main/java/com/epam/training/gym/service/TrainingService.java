package com.epam.training.gym.service;

import com.epam.training.gym.domain.Training;

import java.util.List;

public interface TrainingService {

    Training create(Training training);
    /** @throws java.util.NoSuchElementException if there is no training with this id */
    Training findById(Long id);
    List<Training> findAll();
}
