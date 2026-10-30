package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.epam.training.gym.persistence.storage.StorageNames.TRAINING_STORAGE;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private Map<Long, Training> storage;

    @Autowired
    public void setStorage(@Qualifier(TRAINING_STORAGE) Map<Long, Training> storage) {
        this.storage = storage;
    }

    @Override
    public synchronized Training save(Training training) {
        training.setId(IdGenerator.nextId(storage));
        storage.put(training.getId(), training);

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Training> findAll() {
        return new ArrayList<>(storage.values());
    }
}
