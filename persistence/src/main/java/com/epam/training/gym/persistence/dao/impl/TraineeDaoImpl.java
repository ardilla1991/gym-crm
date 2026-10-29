package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.epam.training.gym.persistence.storage.StorageNames.TRAINEE_STORAGE;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private Map<Long, Trainee> storage;

    @Autowired
    public void setStorage(@Qualifier(TRAINEE_STORAGE) Map<Long, Trainee> storage) {
        this.storage = storage;
    }

    @Override
    public synchronized Trainee save(Trainee trainee) {
        trainee.setId(IdGenerator.nextId(storage));
        storage.put(trainee.getId(), trainee);

        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (trainee.getId() == null || !storage.containsKey(trainee.getId())) {
            throw new IllegalArgumentException("Trainee with id " + trainee.getId() + " not found!");
        }
        storage.put(trainee.getId(), trainee);

        return trainee;
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return new ArrayList<>(storage.values());
    }

}
