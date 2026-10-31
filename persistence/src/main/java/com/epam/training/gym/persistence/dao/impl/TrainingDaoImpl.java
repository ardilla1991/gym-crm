package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.training.gym.persistence.storage.StorageNames.TRAINING_STORAGE;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private Map<Long, Training> storage;

    /** Last assigned id; incrementAndGet() gives the next one without locking. */
    private final AtomicLong lastId = new AtomicLong();

    @Autowired
    public void setStorage(@Qualifier(TRAINING_STORAGE) Map<Long, Training> storage) {
        this.storage = storage;
        // the storage is already filled from the file here, so the counter continues after the loaded ids
        lastId.set(IdGenerator.maxId(storage));
    }

    @Override
    public Training save(Training training) {
        training.setId(lastId.incrementAndGet());
        storage.put(training.getId(), training);
        log.debug("Saved training with id={}", training.getId());

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /** A copy, so callers cannot change the storage through the returned list. */
    @Override
    public List<Training> findAll() {
        return new ArrayList<>(storage.values());
    }
}
