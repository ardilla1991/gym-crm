package com.epam.training.gym.service;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.exception.EntityNotFoundException;

import java.util.List;

public interface TrainingService {

    Training create(Training training);

    /**
     * The trainee adds a training with the given trainer.
     * From the training only the name, date and duration are used;
     * the training type is the trainer's specialization.
     * If the trainer is not in the trainee's list yet, the trainer is added to it.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the trainee's credentials are wrong
     * @throws com.epam.training.gym.service.exception.EntityNotFoundException if the trainer does not exist
     * @throws jakarta.validation.ConstraintViolationException if a required field is missing
     */
    Training addTraining(Credentials traineeCredentials, String trainerUsername, Training training);

    /**
     * All training types. Any signed-in user, trainee or trainer, may get them.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the credentials are wrong
     */
    List<TrainingType> getTrainingTypes(Credentials credentials);
}
