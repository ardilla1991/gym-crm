package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.training.gym.persistence.storage.StorageNames.TRAINER_STORAGE;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private Map<Long, Trainer> storage;

    /** Last assigned id; incrementAndGet() gives the next one without locking. */
    private final AtomicLong lastId = new AtomicLong();

    @Autowired
    public void setStorage(@Qualifier(TRAINER_STORAGE) Map<Long, Trainer> storage) {
        this.storage = storage;
        // the storage is already filled from the file here, so the counter continues after the loaded ids
        lastId.set(IdGenerator.maxId(storage));
    }

    @Override
    public Trainer save(Trainer trainer) {
        trainer.setId(lastId.incrementAndGet());
        storage.put(trainer.getId(), trainer);
        log.debug("Saved trainer with id={}", trainer.getId());

        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (trainer.getId() == null || !storage.containsKey(trainer.getId())) {
            throw new IllegalArgumentException("Trainer with id " + trainer.getId() + " not found!");
        }
        storage.put(trainer.getId(), trainer);
        log.debug("Updated trainer with id={}", trainer.getId());

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /** A copy, so callers cannot change the storage through the returned list. */
    @Override
    public List<Trainer> findAll() {
        return new ArrayList<>(storage.values());
    }
}
