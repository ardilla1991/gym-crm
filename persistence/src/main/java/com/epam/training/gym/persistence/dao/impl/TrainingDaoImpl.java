package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.persistence.dao.TrainingDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public Training save(Training training) {
        entityManager.persist(training);
        log.debug("Training saved: id={}", training.getId());

        return training;
    }
}
