package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.*;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.TrainingService;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.validation.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TraineeService traineeService;
    private TrainerDao trainerDao;
    private TrainingDao trainingDao;
    private TrainingTypeDao trainingTypeDao;
    private EntityValidator entityValidator;

    private UserAuthenticator userAuthenticator;

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
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
    public void setEntityValidator(EntityValidator entityValidator) {
        this.entityValidator = entityValidator;
    }

    @Autowired
    public void setUserAuthenticator(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    @Override
    @Transactional
    public Training addTraining(Credentials traineeCredentials, String trainerUsername, Training training) {
        Trainee trainee = traineeService.authenticate(traineeCredentials);
        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", trainerUsername));

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        // The request has no training type: a training has the type of the trainer's specialization
        training.setTrainingType(trainer.getSpecialization());
        entityValidator.validate(training);

        // A trainee who trains with a trainer has this trainer in the list
        trainee.getTrainers().add(trainer);

        Training saved = trainingDao.save(training);
        log.info("Training added: id={}, trainee={}, trainer={}",
                saved.getId(), traineeCredentials.username(), trainerUsername);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getTrainingTypes(Credentials credentials) {
        userAuthenticator.authenticate(credentials);

        return trainingTypeDao.findAll();
    }

}
