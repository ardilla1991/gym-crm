package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
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
    private TrainingDao trainingDao;
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
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
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

    @Override
    @Transactional
    public Trainer update(Credentials credentials, Trainer newData) {
        // Only the user part is validated: the specialization is not changed, so it may be missing here
        entityValidator.validate(newData.getUser());
        Trainer trainer = authenticate(credentials);

        trainer.getUser().setFirstName(newData.getUser().getFirstName());
        trainer.getUser().setLastName(newData.getUser().getLastName());

        log.info("Trainer profile updated: username={}", credentials.username());

        return trainer;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        authenticate(credentials);
        TrainerTrainingCriteria filters = criteria != null ? criteria : TrainerTrainingCriteria.builder().build();
        List<Training> trainings = trainingDao.findTrainerTrainings(credentials.username(), filters);
        log.info("Trainer trainings found: username={}, count={}", credentials.username(), trainings.size());

        return trainings;
    }

    private TrainingType findTrainingType(TrainingTypeName name) {
        return trainingTypeDao.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Training type", name));
    }
}
