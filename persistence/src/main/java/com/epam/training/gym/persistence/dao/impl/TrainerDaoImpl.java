package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.epam.training.gym.persistence.storage.StorageNames.TRAINER_STORAGE;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private Map<Long, Trainer> storage;

    @Autowired
    public void setStorage(@Qualifier(TRAINER_STORAGE) Map<Long, Trainer> storage) {
        this.storage = storage;
    }

    @Override
    public synchronized Trainer save(Trainer trainer) {
        trainer.setId(IdGenerator.nextId(storage));
        storage.put(trainer.getId(), trainer);

        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (trainer.getId() == null || !storage.containsKey(trainer.getId())) {
            throw new IllegalArgumentException("Trainer with id " + trainer.getId() + " not found!");
        }
        storage.put(trainer.getId(), trainer);

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainer> findAll() {
        return new ArrayList<>(storage.values());
    }
}
