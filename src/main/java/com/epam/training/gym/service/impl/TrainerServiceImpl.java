package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.service.TrainerService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import com.epam.training.gym.service.validation.TrainingPeriodValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl extends AbstractProfileService<Trainer>
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
    protected Optional<Trainer> findByUsername(String username) {
        return trainerDao.findByUsername(username);
    }

    @Override
    protected Optional<Trainer> findProfileByUsername(String username) {
        return trainerDao.findByUsernameWithTrainees(username);
    }

    @Override
    @Transactional
    public Trainer create(Trainer trainer) {
        entityValidator.validate(trainer);
        // Only the name of the specialization comes from the caller: the type itself is loaded from the database
        trainer.setSpecialization(findTrainingType(trainer.getSpecialization().getTrainingTypeName()));
        newUserDataGenerator.fillNewUserData(trainer.getUser());
        Trainer created = trainerDao.save(trainer);
        log.info("Trainer created: id={}, username={}", created.getId(), created.getUser().getUsername());

        // The DAO never returns the password; registration returns the generated one to the new user
        created.getUser().setPassword(trainer.getUser().getPassword());
        return created;
    }

    @Override
    @Transactional
    public Trainer update(Credentials credentials, Trainer newData) {
        // Loads the trainer with the trainees list: the response returns the whole profile
        Trainer trainer = authenticateWithDetails(credentials);
        // Only the user part is validated: the specialization is not changed, so it may be missing here
        entityValidator.validate(newData.getUser());

        // The specialization is read-only
        trainer.getUser().setFirstName(newData.getUser().getFirstName());
        trainer.getUser().setLastName(newData.getUser().getLastName());
        trainer.getUser().setActive(newData.getUser().isActive());
        trainerDao.update(trainer);
        log.info("Trainer profile updated: username={}", credentials.username());

        return trainer;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        authenticate(credentials);
        TrainerTrainingCriteria filters = criteria != null ? criteria : TrainerTrainingCriteria.builder().build();
        TrainingPeriodValidator.validate(filters.fromDate(), filters.toDate());
        List<Training> trainings = trainingDao.findTrainerTrainings(credentials.username(), filters);
        log.info("Trainer trainings found: username={}, count={}", credentials.username(), trainings.size());

        return trainings;
    }

    private TrainingType findTrainingType(TrainingTypeName name) {
        return trainingTypeDao.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Training type", name));
    }
}
