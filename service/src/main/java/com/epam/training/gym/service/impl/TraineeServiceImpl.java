package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

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
        Trainee saved;
        // Generating the username and saving must be one step: otherwise two profiles created at the same time
        // (a trainee and a trainer too) could get the same username. The generator is a single bean shared
        // by both services, so it is used as their common lock.
        synchronized (newUserDataGenerator) {
            newUserDataGenerator.fillNewUserData(trainee);
            saved = traineeDao.save(trainee);
        }
        log.info("Trainee created: id={}, username={}", saved.getId(), saved.getUsername());

        return saved;
    }

    /** Keeps userId, username and password of the stored trainee: they are generated, not editable. */
    @Override
    public Trainee update(Trainee trainee) {
        Objects.requireNonNull(trainee.getId(), "Trainee id is required for update");
        Trainee existing = findById(trainee.getId());
        trainee.setUserId(existing.getUserId());
        trainee.setUsername(existing.getUsername());
        trainee.setPassword(existing.getPassword());

        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee updated: id={}, username={}", updated.getId(), updated.getUsername());

        return updated;
    }

    @Override
    public void delete(Long id) {
        if (traineeDao.findById(id).isEmpty()) {
            log.warn("Trainee with id={} not found, nothing to delete", id);
            return;
        }

        traineeDao.delete(id);
        log.info("Trainee deleted: id={}", id);
    }

    @Override
    public Trainee findById(Long id) {
        log.debug("Looking for trainee id={}", id);

        return traineeDao.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException("Trainee", id));
    }

    @Override
    public List<Trainee> findAll() {
        log.debug("Looking for all trainees");

        return traineeDao.findAll();
    }
}
