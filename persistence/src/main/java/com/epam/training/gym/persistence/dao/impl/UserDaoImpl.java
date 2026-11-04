package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.persistence.dao.UserDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<String> findUsernamesStartingWith(String prefix) {
        return entityManager
                .createQuery("SELECT u.username FROM User u WHERE u.username LIKE :prefix", String.class)
                .setParameter("prefix", prefix + "%")
                .getResultList();
    }
}
