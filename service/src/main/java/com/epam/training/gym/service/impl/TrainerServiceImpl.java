package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.service.TrainerService;
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
public class TrainerServiceImpl extends AbstractProfileService<Trainer>
        implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private NewUserDataGenerator newUserDataGenerator;
    private EntityValidator entityValidator;

    public TrainerServiceImpl() {
        super("Trainer");
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
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
    protected Optional<Trainer> findByUsername(String username) {
        return trainerDao.findByUsername(username);
    }

    @Override
    @Transactional
    public Trainer create(Trainer trainer) {
        entityValidator.validate(trainer);
        trainer.setSpecialization(findTrainingType(trainer.getSpecialization().getTrainingTypeName()));
        newUserDataGenerator.fillNewUserData(trainer.getUser());
        Trainer saved = trainerDao.save(trainer);
        log.info("Trainer created: id={}, username={}", saved.getId(), saved.getUser().getUsername());

        return saved;
    }

    /*
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
    
     */

    private TrainingType findTrainingType(TrainingTypeName name) {
        return trainingTypeDao.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Training type", name));
    }
}
