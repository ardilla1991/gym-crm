package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.domain.criteria.TrainerTrainingCriteria;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {

    Training save(Training training);

    /** Trainings of the trainee, ordered by date. */
    List<Training> findTraineeTrainings(String traineeUsername, TraineeTrainingCriteria criteria);

    /** Trainings of the trainer, ordered by date. */
    List<Training> findTrainerTrainings(String trainerUsername, TrainerTrainingCriteria criteria);
}
