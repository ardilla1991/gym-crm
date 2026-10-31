package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    /** Assigns a new id to the trainer and stores it. */
    Trainer save(Trainer trainer);
    /** Replaces the stored trainer with the same id. */
    Trainer update(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
}
