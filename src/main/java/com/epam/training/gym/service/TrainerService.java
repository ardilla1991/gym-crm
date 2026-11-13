package com.epam.training.gym.service;

import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;

import java.util.List;

public interface TrainerService extends AccountService<Trainer> {
    /**
     * Creates a trainer profile with a generated username and password.
     * Only the name of the specialization is used: the type itself is taken from the database.
     * @throws jakarta.validation.ConstraintViolationException if a required field is missing
     * @throws com.epam.training.gym.service.exception.EntityNotFoundException if the training type does not exist
     */
    Trainer create(Trainer trainer);

    /**
     * Updates first name and last name.
     * The specialization is read-only: past trainings refer to it.
     * @throws jakarta.validation.ConstraintViolationException if a required field is missing
     */
    Trainer update(Credentials credentials, Trainer newData);

    /** The trainer's trainings; null criteria means no filters. */
    List<Training> getTrainings(Credentials credentials, TrainerTrainingCriteria criteria);
}
