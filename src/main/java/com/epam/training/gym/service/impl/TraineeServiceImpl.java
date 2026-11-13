package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.mapper.TraineeEntityMapper;
import com.epam.training.gym.service.mapper.TrainerEntityMapper;
import com.epam.training.gym.service.mapper.TrainingEntityMapper;
import com.epam.training.gym.service.validation.EntityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TraineeServiceImpl extends AbstractProfileService<Trainee, TraineeEntity>
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
        TraineeEntity saved = traineeDao.save(TraineeEntityMapper.toNewEntity(trainee));
        log.info("Trainee created: id={}, username={}", saved.getId(), saved.getUser().getUsername());

        return TraineeEntityMapper.toModel(saved);
    }

    @Override
    protected Optional<TraineeEntity> findByUsername(String username) {
        return traineeDao.findByUsername(username);
    }

    @Override
    protected Optional<TraineeEntity> findProfileByUsername(String username) {
        return traineeDao.findByUsernameWithTrainers(username);
    }

    @Override
    protected Trainee toModel(TraineeEntity entity) {
        return TraineeEntityMapper.toModel(entity);
    }

    @Override
    protected Trainee toProfileModel(TraineeEntity entity) {
        return TraineeEntityMapper.toModelWithTrainers(entity);
    }

    @Override
    @Transactional
    public Trainee update(Credentials credentials, Trainee newData) {
        entityValidator.validate(newData);
        TraineeEntity trainee = authenticateEntity(credentials);

        // Only these fields come from the caller; id, username, password and status stay as they are.
        // The entity is managed: Hibernate writes the changes at commit (dirty checking)
        trainee.getUser().setFirstName(newData.getUser().getFirstName());
        trainee.getUser().setLastName(newData.getUser().getLastName());
        trainee.setDateOfBirth(newData.getDateOfBirth());
        trainee.setAddress(newData.getAddress());

        log.info("Trainee profile updated: username={}", credentials.username());

        return TraineeEntityMapper.toModel(trainee);
    }

    @Override
    @Transactional
    public void delete(Credentials credentials) {
        traineeDao.delete(authenticateEntity(credentials));
        log.info("Trainee deleted: username={}", credentials.username());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        authenticateEntity(credentials);
        TraineeTrainingCriteria filters = criteria != null ? criteria : TraineeTrainingCriteria.builder().build();
        List<Training> trainings = trainingDao.findTraineeTrainings(credentials.username(), filters).stream()
                .map(TrainingEntityMapper::toModel)
                .toList();
        log.info("Trainee trainings found: username={}, count={}", credentials.username(), trainings.size());

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(Credentials credentials) {
        authenticateEntity(credentials);

        return trainerDao.findNotAssignedToTrainee(credentials.username()).stream()
                .map(TrainerEntityMapper::toModel)
                .toList();
    }

    @Override
    @Transactional
    public List<Trainer> updateTrainers(Credentials credentials, List<String> trainerUsernames) {
        TraineeEntity trainee = authenticateEntity(credentials);

        Set<String> requested = new HashSet<>(trainerUsernames);
        List<TrainerEntity> trainers = trainerDao.findByUsernames(requested);
        if (trainers.size() != requested.size()) {
            trainers.forEach(trainer -> requested.remove(trainer.getUser().getUsername()));
            throw new EntityNotFoundException("Trainer", requested);
        }

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        log.info("Trainee trainers updated: username={}, trainers={}", credentials.username(), trainerUsernames);

        return trainers.stream()
                .map(TrainerEntityMapper::toModel)
                .toList();
    }
}
