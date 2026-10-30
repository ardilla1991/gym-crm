package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public Training create(Training training) {
        Training saved = trainingDao.save(training);
        log.info("Training created: id={}, name={}", saved.getId(), saved.getTrainingName());

        return saved;
    }

    @Override
    public Training findById(Long id) {
        log.debug("Looking for training id={}", id);

        return trainingDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Training with id " + id + " not found"));
    }

    @Override
    public List<Training> findAll() {
        log.debug("Looking for all trainings");

        return trainingDao.findAll();
    }
}
