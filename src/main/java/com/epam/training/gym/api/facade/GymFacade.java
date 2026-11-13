package com.epam.training.gym.api.facade;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.domain.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.TrainerService;
import com.epam.training.gym.service.TrainingService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Single entry point to the gym CRM: delegates every call to the matching service.
 * Contains no business logic of its own.
 */
@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(Trainee trainee) {
        return traineeService.create(trainee);
    }

    public boolean matchTraineeCredentials(Credentials credentials) {
        return traineeService.matchCredentials(credentials);
    }

    public boolean matchTrainerCredentials(Credentials credentials) {
        return trainerService.matchCredentials(credentials);
    }

    public void changeTraineePassword(Credentials credentials, String newPassword) {
        traineeService.changePassword(credentials, newPassword);
    }

    public void changeTrainerPassword(Credentials credentials, String newPassword) {
        trainerService.changePassword(credentials, newPassword);
    }

    public boolean toggleTraineeActive(Credentials credentials) {
        return traineeService.toggleActive(credentials);
    }

    public boolean toggleTrainerActive(Credentials credentials) {
        return trainerService.toggleActive(credentials);
    }

    public Trainee getTraineeProfile(Credentials credentials, String username) {
        return traineeService.getProfile(credentials, username);
    }

    public Trainer getTrainerProfile(Credentials credentials, String username) {
        return trainerService.getProfile(credentials, username);
    }

    public Trainer createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    public Trainee updateTrainee(Credentials credentials, Trainee newData) {
        return traineeService.update(credentials, newData);
    }

    public Trainer updateTrainer(Credentials credentials, Trainer newData) {
        return trainerService.update(credentials, newData);
    }

    public void deleteTrainee(Credentials credentials) {
        traineeService.delete(credentials);
    }

    public List<Training> getTraineeTrainings(Credentials credentials, TraineeTrainingCriteria criteria) {
        return traineeService.getTrainings(credentials, criteria);
    }

    public List<Training> getTrainerTrainings(Credentials credentials, TrainerTrainingCriteria criteria) {
        return trainerService.getTrainings(credentials, criteria);
    }

    public Training addTraining(Credentials traineeCredentials, String trainerUsername, Training training) {
        return trainingService.addTraining(traineeCredentials, trainerUsername, training);
    }

    public List<Trainer> getUnassignedTrainers(Credentials credentials) {
        return traineeService.getUnassignedTrainers(credentials);
    }

    public List<Trainer> updateTraineeTrainers(Credentials credentials, List<String> trainerUsernames) {
        return traineeService.updateTrainers(credentials, trainerUsernames);
    }

}
