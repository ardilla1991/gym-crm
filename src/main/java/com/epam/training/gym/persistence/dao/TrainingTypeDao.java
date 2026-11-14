package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDao {

    Optional<TrainingTypeEntity> findByName(TrainingTypeName name);

    /** All training types, ordered by name. */
    List<TrainingTypeEntity> findAll();
}