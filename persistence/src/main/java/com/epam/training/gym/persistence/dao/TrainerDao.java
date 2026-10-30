package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer save(Trainer trainer);
    Trainer update(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
}
