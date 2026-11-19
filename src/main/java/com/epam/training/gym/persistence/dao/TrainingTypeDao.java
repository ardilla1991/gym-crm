package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.TrainingTypeName;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {

    Optional<TrainingType> findByName(TrainingTypeName name);

    /** All training types, ordered by name. */
    List<TrainingType> findAll();
}
