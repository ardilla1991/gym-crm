package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.service.TrainerService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.mapper.TrainerEntityMapper;
import com.epam.training.gym.service.mapper.TrainingEntityMapper;
import com.epam.training.gym.service.validation.EntityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl extends AbstractProfileService<Trainer, TrainerEntity>
        implements TrainerService {

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
    protected Optional<TrainerEntity> findByUsername(String username) {
        return trainerDao.findByUsername(username);
    }

    @Override
    protected Optional<TrainerEntity> findProfileByUsername(String username) {
        return trainerDao.findByUsernameWithTrainees(username);
    }

    @Override
    protected Trainer toModel(TrainerEntity entity) {
        return TrainerEntityMapper.toModel(entity);
    }

    @Override
    protected Trainer toProfileModel(TrainerEntity entity) {
        return TrainerEntityMapper.toModelWithTrainees(entity);
    }

    @Override
    @Transactional
    public Trainer create(Trainer trainer) {
        entityValidator.validate(trainer);
        // Only the name of the specialization comes from the caller: the type itself is loaded from the database
        TrainingTypeEntity specialization = findTrainingType(trainer.getSpecialization().getTrainingTypeName());
        newUserDataGenerator.fillNewUserData(trainer.getUser());
        TrainerEntity saved = trainerDao.save(TrainerEntityMapper.toNewEntity(trainer, specialization));
        log.info("Trainer created: id={}, username={}", saved.getId(), saved.getUser().getUsername());

        return TrainerEntityMapper.toModel(saved);
    }

    @Override
    @Transactional
    public Trainer update(Credentials credentials, Trainer newData) {
        // Only the user part is validated: the specialization is not changed, so it may be missing here
        entityValidator.validate(newData.getUser());
        TrainerEntity trainer = authenticateEntity(credentials);

        // The entity is managed: Hibernate writes the changes at commit (dirty checking)
        trainer.getUser().setFirstName(newData.getUser().getFirstName());
        trainer.getUser().setLastName(newData.getUser().getLastName());
        log.info("Trainer profile updated: username={}", credentials.username());

        return TrainerEntityMapper.toModel(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        authenticateEntity(credentials);
        TrainerTrainingCriteria filters = criteria != null ? criteria : TrainerTrainingCriteria.builder().build();
        List<Training> trainings = trainingDao.findTrainerTrainings(credentials.username(), filters).stream()
                .map(TrainingEntityMapper::toModel)
                .toList();
        log.info("Trainer trainings found: username={}, count={}", credentials.username(), trainings.size());

        return trainings;
    }

    private TrainingTypeEntity findTrainingType(TrainingTypeName name) {
        return trainingTypeDao.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Training type", name));
    }
}
