package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training save(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
}
