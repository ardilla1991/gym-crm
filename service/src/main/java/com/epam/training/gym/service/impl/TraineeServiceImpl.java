package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TraineeServiceImpl extends AbstractProfileService<Trainee>
        implements TraineeService {

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingDao trainingDao;
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
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
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

    @Override
    @Transactional
    public Trainee update(Credentials credentials, Trainee newData) {
        entityValidator.validate(newData);
        Trainee trainee = authenticate(credentials);

        // Only these fields come from the caller; id, username, password and status stay as they are
        trainee.getUser().setFirstName(newData.getUser().getFirstName());
        trainee.getUser().setLastName(newData.getUser().getLastName());
        trainee.setDateOfBirth(newData.getDateOfBirth());
        trainee.setAddress(newData.getAddress());

        log.info("Trainee profile updated: username={}", credentials.username());

        return trainee;
    }

    @Override
    @Transactional
    public void delete(Credentials credentials) {
        Trainee trainee = authenticate(credentials);
        traineeDao.delete(trainee);
        log.info("Trainee deleted: username={}", credentials.username());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        authenticate(credentials);
        TraineeTrainingCriteria filters = criteria != null ? criteria : TraineeTrainingCriteria.builder().build();
        List<Training> trainings = trainingDao.findTraineeTrainings(credentials.username(), filters);
        log.info("Trainee trainings found: username={}, count={}", credentials.username(), trainings.size());

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(Credentials credentials) {
        authenticate(credentials);

        return trainerDao.findNotAssignedToTrainee(credentials.username());
    }

    @Override
    @Transactional
    public List<Trainer> updateTrainers(Credentials credentials, List<String> trainerUsernames) {
        Trainee trainee = authenticate(credentials);

        Set<String> requested = new HashSet<>(trainerUsernames);
        List<Trainer> trainers = trainerDao.findByUsernames(requested);
        if (trainers.size() != requested.size()) {
            trainers.forEach(trainer -> requested.remove(trainer.getUser().getUsername()));
            throw new EntityNotFoundException("Trainer", requested);
        }

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        log.info("Trainee trainers updated: username={}, trainers={}", credentials.username(), trainerUsernames);

        return trainers;
    }
}
