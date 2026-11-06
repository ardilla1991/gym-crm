package com.epam.training.gym.api.facade;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.auth.Credentials;
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

    /*public Trainee updateTrainee(Trainee trainee) {
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    public Trainee findTrainee(Long id) {
        return traineeService.findById(id);
    }

    public List<Trainee> findAllTrainees() {
        return traineeService.findAll();
    }*/

    public Trainer createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    /*public Trainer updateTrainer(Trainer trainer) {
        return trainerService.update(trainer);
    }

    public Trainer findTrainer(Long id) {
        return trainerService.findById(id);
    }

    public List<Trainer> findAllTrainers() {
        return trainerService.findAll();
    }

    public Training createTraining(Training training) {
        return trainingService.create(training);
    }

    public Training findTraining(Long id) {
        return trainingService.findById(id);
    }

    public List<Training> findAllTrainings() {
        return trainingService.findAll();
    }*/

}
