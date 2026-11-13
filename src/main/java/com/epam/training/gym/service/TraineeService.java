package com.epam.training.gym.service;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.service.exception.EntityNotFoundException;

import java.util.List;

public interface TraineeService extends AccountService<Trainee> {
    /**
     * Creates a trainee profile with a generated username and password.
     * @return the saved trainee; its user holds the generated username and password
     * @throws jakarta.validation.ConstraintViolationException if a required field is missing
     */
    Trainee create(Trainee trainee);

    /**
     * Updates first name, last name, date of birth and address.
     * Username, password and status are not changed here.
     * @throws jakarta.validation.ConstraintViolationException if a required field is missing
     */
    Trainee update(Credentials credentials, Trainee newData);

    /** Hard delete; the trainee's trainings are deleted too. */
    void delete(Credentials credentials);

    /** The trainee's trainings; null criteria means no filters. */
    List<Training> getTrainings(Credentials credentials, TraineeTrainingCriteria criteria);

    /** Trainers that are not in the trainee's list. */
    List<Trainer> getUnassignedTrainers(Credentials credentials);

    /**
     * Replaces the trainee's trainers list.
     * @return the trainers now assigned to the trainee
     * @throws com.epam.training.gym.service.exception.EntityNotFoundException if any username is unknown;
     *         the list is not changed then
     */
    List<Trainer> updateTrainers(Credentials credentials, List<String> trainerUsernames);
}
