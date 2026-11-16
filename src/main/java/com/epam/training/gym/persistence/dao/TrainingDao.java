package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;

import java.util.List;

public interface TrainingDao {

    /** The trainee, the trainer and the type must already exist: they are linked by their ids. */
    Training save(Training training);

    /** Trainings of the trainee, ordered by date. */
    List<Training> findTraineeTrainings(String traineeUsername, TraineeTrainingCriteria criteria);

    /** Trainings of the trainer, ordered by date. */
    List<Training> findTrainerTrainings(String trainerUsername, TrainerTrainingCriteria criteria);
}
