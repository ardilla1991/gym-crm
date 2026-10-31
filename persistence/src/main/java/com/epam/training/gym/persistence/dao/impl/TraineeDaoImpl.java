package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.training.gym.persistence.storage.StorageNames.TRAINEE_STORAGE;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private Map<Long, Trainee> storage;

    /** Last assigned id; incrementAndGet() gives the next one without locking. */
    private final AtomicLong lastId = new AtomicLong();

    @Autowired
    public void setStorage(@Qualifier(TRAINEE_STORAGE) Map<Long, Trainee> storage) {
        this.storage = storage;
        // the storage is already filled from the file here, so the counter continues after the loaded ids
        lastId.set(IdGenerator.maxId(storage));
    }

    @Override
    public Trainee save(Trainee trainee) {
        trainee.setId(lastId.incrementAndGet());
        storage.put(trainee.getId(), trainee);
        log.debug("Saved trainee with id={}", trainee.getId());

        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (trainee.getId() == null || !storage.containsKey(trainee.getId())) {
            throw new IllegalArgumentException("Trainee with id " + trainee.getId() + " not found!");
        }
        storage.put(trainee.getId(), trainee);
        log.debug("Updated trainee with id={}", trainee.getId());

        return trainee;
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
        log.debug("Deleted trainee with id={}", id);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /** A copy, so callers cannot change the storage through the returned list. */
    @Override
    public List<Trainee> findAll() {
        return new ArrayList<>(storage.values());
    }

}
