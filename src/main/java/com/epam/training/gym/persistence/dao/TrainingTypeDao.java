package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;

import java.util.Optional;

public interface TrainingTypeDao {
    Optional<TrainingType> findByName(TrainingTypeName name);
}
