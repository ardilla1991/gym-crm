package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidInputException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        Trainee created = traineeDao.save(trainee);
        log.info("Trainee created: id={}, username={}", created.getId(), created.getUser().getUsername());

        // The DAO never returns the password; registration returns the generated one to the new user
        created.getUser().setPassword(trainee.getUser().getPassword());
        return created;
    }

    @Override
    protected Optional<Trainee> findByUsername(String username) {
        return traineeDao.findByUsername(username);
    }

    @Override
    protected Optional<Trainee> findProfileByUsername(String username) {
        return traineeDao.findByUsernameWithTrainers(username);
    }

    @Override
    @Transactional
    public Trainee update(Credentials credentials, Trainee newData) {
        // Loads the trainee with the trainers list: the response returns the whole profile
        Trainee trainee = authenticateWithDetails(credentials);
        entityValidator.validate(newData);

        // Only these fields come from the caller; id, username and password stay as they are
        trainee.getUser().setFirstName(newData.getUser().getFirstName());
        trainee.getUser().setLastName(newData.getUser().getLastName());
        trainee.getUser().setActive(newData.getUser().isActive());
        trainee.setDateOfBirth(newData.getDateOfBirth());
        trainee.setAddress(newData.getAddress());

        traineeDao.update(trainee);
        log.info("Trainee profile updated: username={}", credentials.username());

        return trainee;
    }

    @Override
    @Transactional
    public void delete(Credentials credentials) {
        traineeDao.delete(authenticate(credentials));
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
        List<Trainer> trainers = trainerDao.findActiveNotAssignedToTrainee(credentials.username());
        log.info("Unassigned trainers found: username={}, count={}", credentials.username(), trainers.size());

        return trainers;
    }

    @Override
    @Transactional
    public List<Trainer> updateTrainers(Credentials credentials, List<String> trainerUsernames) {
        Trainee trainee = authenticate(credentials);

        Set<String> requested = validTrainerUsernames(trainerUsernames);
        List<Trainer> trainers = trainerDao.findByUsernames(requested);
        if (trainers.size() != requested.size()) {
            trainers.forEach(trainer -> requested.remove(trainer.getUser().getUsername()));
            throw new EntityNotFoundException("Trainer", requested);
        }

        traineeDao.replaceTrainers(trainee, trainers);
        log.info("Trainee trainers updated: username={}, trainers={}", credentials.username(), trainerUsernames);

        return trainers;
    }

    /** The list must not be null or empty and must have no blank or repeated usernames. */
    private static Set<String> validTrainerUsernames(List<String> trainerUsernames) {
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            throw new InvalidInputException("Trainers list must not be empty");
        }
        if (trainerUsernames.stream().anyMatch(username -> username == null || username.isBlank())) {
            throw new InvalidInputException("Trainer username must not be blank");
        }
        Set<String> unique = new HashSet<>(trainerUsernames);
        if (unique.size() != trainerUsernames.size()) {
            throw new InvalidInputException("Trainer usernames must not repeat: " + trainerUsernames);
        }
        return unique;
    }
}
