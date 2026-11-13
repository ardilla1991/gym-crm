package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.TrainingService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidStateException;
import com.epam.training.gym.service.mapper.TraineeEntityMapper;
import com.epam.training.gym.service.mapper.TrainerEntityMapper;
import com.epam.training.gym.service.mapper.TrainingEntityMapper;
import com.epam.training.gym.service.mapper.TrainingTypeEntityMapper;
import com.epam.training.gym.service.validation.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TraineeService traineeService;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingDao trainingDao;
    private TrainingTypeDao trainingTypeDao;
    private EntityValidator entityValidator;

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
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
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setEntityValidator(EntityValidator entityValidator) {
        this.entityValidator = entityValidator;
    }

    @Override
    @Transactional
    public Training addTraining(Credentials traineeCredentials, String trainerUsername, Training training) {
        // The password check stays in one place: the trainee service
        traineeService.authenticate(traineeCredentials);
        // The trainee exists (just authenticated); its row is needed to link the training to it
        TraineeEntity trainee = traineeDao.findByUsername(traineeCredentials.username()).orElseThrow();
        TrainerEntity trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer", trainerUsername));
        if (!trainer.getUser().isActive()) {
            throw new InvalidStateException("Trainer " + trainerUsername + " is not active");
        }
        TrainingTypeEntity trainingType = training.getTrainingType() != null
                ? findTrainingType(training.getTrainingType().getTrainingTypeName())
                : null;

        // The model is validated complete: with the trainee, the trainer and the type found above
        training.setTrainee(TraineeEntityMapper.toModel(trainee));
        training.setTrainer(TrainerEntityMapper.toModel(trainer));
        training.setTrainingType(trainingType != null ? TrainingTypeEntityMapper.toModel(trainingType) : null);
        entityValidator.validate(training);

        // A trainee who trains with a trainer has this trainer in the list
        trainee.getTrainers().add(trainer);
        TrainingEntity saved = trainingDao.save(
                TrainingEntityMapper.toNewEntity(training, trainee, trainer, trainingType));
        log.info("Training added: id={}, trainee={}, trainer={}",
                saved.getId(), traineeCredentials.username(), trainerUsername);

        return TrainingEntityMapper.toModel(saved);
    }

    private TrainingTypeEntity findTrainingType(TrainingTypeName name) {
        return trainingTypeDao.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Training type", name));
    }
}
