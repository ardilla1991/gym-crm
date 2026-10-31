package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.service.TrainerService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

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
        Trainer saved;
        // Generating the username and saving must be one step: otherwise two profiles created at the same time
        // (a trainee and a trainer too) could get the same username. The generator is a single bean shared
        // by both services, so it is used as their common lock.
        synchronized (newUserDataGenerator) {
            newUserDataGenerator.fillNewUserData(trainer);
            saved = trainerDao.save(trainer);
        }
        log.info("Trainer created: id={}, username={}", saved.getId(), saved.getUsername());

        return saved;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Objects.requireNonNull(trainer.getId(), "Trainer id is required for update");
        Trainer existing = findById(trainer.getId());
        trainer.setUserId(existing.getUserId());
        trainer.setUsername(existing.getUsername());
        trainer.setPassword(existing.getPassword());

        Trainer updated = trainerDao.update(trainer);
        log.info("Trainer updated: id={}, username={}", updated.getId(), updated.getUsername());

        return updated;
    }

    @Override
    public Trainer findById(Long id) {
        log.debug("Looking for trainer id={}", id);

        return trainerDao.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Trainer", id));
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Looking for all trainers");

        return trainerDao.findAll();
    }
}
