package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.entity.TrainingEntity;

import java.util.List;

public interface TrainingDao {

    TrainingEntity save(TrainingEntity training);

    /** Trainings of the trainee, ordered by date. */
    List<TrainingEntity> findTraineeTrainings(String traineeUsername, TraineeTrainingCriteria criteria);

    /** Trainings of the trainer, ordered by date. */
    List<TrainingEntity> findTrainerTrainings(String trainerUsername, TrainerTrainingCriteria criteria);
}