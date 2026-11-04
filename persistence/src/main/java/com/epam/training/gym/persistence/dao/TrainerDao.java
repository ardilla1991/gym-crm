package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Trainer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TrainerDao {

    Trainer save(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    /** For updating a trainee's trainers list by trainer usernames. */
    List<Trainer> findByUsernames(Collection<String> usernames);
}
