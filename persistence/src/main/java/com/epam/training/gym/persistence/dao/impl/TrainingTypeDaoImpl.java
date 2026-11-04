package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<TrainingType> findByName(TrainingTypeName name) {
        return entityManager
                .createQuery("SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }
}
