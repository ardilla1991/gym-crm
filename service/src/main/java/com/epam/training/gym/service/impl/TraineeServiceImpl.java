package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.User;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TraineeServiceImpl extends AbstractProfileService<Trainee>
        implements TraineeService {

    private TraineeDao traineeDao;
    private NewUserDataGenerator newUserDataGenerator;
    private EntityValidator entityValidator;

    public TraineeServiceImpl() {
        super("Trainee");
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setNewUserDataGenerator(NewUserDataGenerator newUserDataGenerator) {
        this.newUserDataGenerator = newUserDataGenerator;
    }

    @Autowired
    public void setEntityValidator(EntityValidator entityValidator) {
        this.entityValidator = entityValidator;
    }

    @Override
    @Transactional
    public Trainee create(Trainee trainee) {
        entityValidator.validate(trainee);
        newUserDataGenerator.fillNewUserData(trainee.getUser());
        Trainee saved = traineeDao.save(trainee);
        log.info("Trainee created: id={}, username={}", saved.getId(), saved.getUser().getUsername());

        return saved;
    }

    @Override
    protected Optional<Trainee> findByUsername(String username) {
        return traineeDao.findByUsername(username);
    }

    /** Keeps userId, username and password of the stored trainee: they are generated, not editable. */
    /*@Override
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
     */
}
