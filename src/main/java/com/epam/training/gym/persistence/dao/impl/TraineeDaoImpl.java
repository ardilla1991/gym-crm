package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public TraineeEntity save(TraineeEntity trainee) {
        entityManager.persist(trainee);       // the trainee's user is saved too: cascade = ALL
        log.debug("Trainee saved: id={}", trainee.getId());

        return trainee;
    }

    @Override
    public Optional<TraineeEntity> findByUsername(String username) {
        return entityManager
                .createQuery("SELECT t FROM Trainee t WHERE t.user.username = :username", TraineeEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void delete(TraineeEntity trainee) {
        entityManager.remove(trainee);
        log.debug("Trainee deleted: id={}", trainee.getId());
    }

    @Override
    public Optional<TraineeEntity> findByUsernameWithTrainers(String username) {
        return entityManager.createQuery("""
                    SELECT t FROM Trainee t
                    JOIN FETCH t.user
                    LEFT JOIN FETCH t.trainers tr
                    LEFT JOIN FETCH tr.user
                    LEFT JOIN FETCH tr.specialization
                    WHERE t.user.username = :username
                    """, TraineeEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }
}