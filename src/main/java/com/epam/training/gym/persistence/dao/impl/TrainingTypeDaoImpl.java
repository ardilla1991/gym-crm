package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
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
    public Optional<TrainingTypeEntity> findByName(TrainingTypeName name) {
        return entityManager
                .createQuery("SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingTypeEntity.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }
}