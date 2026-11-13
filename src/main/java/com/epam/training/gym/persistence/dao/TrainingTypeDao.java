package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;

import java.util.Optional;

public interface TrainingTypeDao {

    Optional<TrainingTypeEntity> findByName(TrainingTypeName name);
}