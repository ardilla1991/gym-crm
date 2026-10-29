package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TraineeServiceImpl implements TraineeService {

    private TraineeDao traineeDao;
    private NewUserDataGenerator newUserDataGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setNewUserDataGenerator(NewUserDataGenerator newUserDataGenerator) {
        this.newUserDataGenerator = newUserDataGenerator;
    }

    @Override
    public Trainee create(Trainee trainee) {
        trainee.setUserId(newUserDataGenerator.nextUserId());
        trainee.setUsername(newUserDataGenerator.generateUsername(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(newUserDataGenerator.generatePassword());
        trainee.setActive(true);

        Trainee saved = traineeDao.save(trainee);

        return saved;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Trainee updated = traineeDao.update(trainee);

        return updated;
    }

    @Override
    public void delete(Long id) {
        traineeDao.delete(id);
    }

    @Override
    public Trainee findById(Long id) {
        return traineeDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + id + " not found"));
    }

    @Override
    public List<Trainee> findAll() {
        return traineeDao.findAll();
    }
}
