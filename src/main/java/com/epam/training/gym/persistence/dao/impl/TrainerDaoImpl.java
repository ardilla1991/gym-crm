package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public TrainerEntity save(TrainerEntity trainer) {
        entityManager.persist(trainer);
        log.debug("Trainer saved: id={}", trainer.getId());

        return trainer;
    }

    @Override
    public Optional<TrainerEntity> findByUsername(String username) {
        return entityManager
                .createQuery("SELECT t FROM Trainer t WHERE t.user.username = :username", TrainerEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<TrainerEntity> findByUsernames(Collection<String> usernames) {
        if (usernames.isEmpty()) {
            return List.of();   // no need to query the database
        }
        return entityManager
                .createQuery("SELECT t FROM Trainer t WHERE t.user.username IN :usernames", TrainerEntity.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }

    @Override
    public List<TrainerEntity> findNotAssignedToTrainee(String traineeUsername) {
        return entityManager.createQuery("""
                    SELECT tr FROM Trainer tr
                    WHERE tr NOT IN (
                        SELECT assigned FROM Trainee te JOIN te.trainers assigned
                        WHERE te.user.username = :username)
                    """, TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    @Override
    public Optional<TrainerEntity> findByUsernameWithTrainees(String username) {
        return entityManager.createQuery("""
                    SELECT t FROM Trainer t
                    JOIN FETCH t.user
                    JOIN FETCH t.specialization
                    LEFT JOIN FETCH t.trainees te
                    LEFT JOIN FETCH te.user
                    WHERE t.user.username = :username
                    """, TrainerEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

}