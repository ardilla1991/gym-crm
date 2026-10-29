package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.service.TrainerService;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainerServiceImpl implements TrainerService {

    private TrainerDao trainerDao;
    private NewUserDataGenerator newUserDataGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setNewUserDataGenerator(NewUserDataGenerator newUserDataGenerator) {
        this.newUserDataGenerator = newUserDataGenerator;
    }

    @Override
    public Trainer create(Trainer trainer) {
        trainer.setUserId(newUserDataGenerator.nextUserId());
        trainer.setUsername(newUserDataGenerator.generateUsername(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(newUserDataGenerator.generatePassword());
        trainer.setActive(true);

        Trainer saved = trainerDao.save(trainer);

        return saved;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Trainer updated = trainerDao.update(trainer);

        return updated;
    }

    @Override
    public Trainer findById(Long id) {
        return trainerDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + id + " not found"));
    }

    @Override
    public List<Trainer> findAll() {
        return trainerDao.findAll();
    }
}
