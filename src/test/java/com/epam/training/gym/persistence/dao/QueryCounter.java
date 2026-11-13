package com.epam.training.gym.persistence.dao;

import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * Counts SQL statements sent to the database from the moment it is created.
 * Needs hibernate.generate_statistics=true.
 */
public class QueryCounter {

    private final Statistics statistics;

    public QueryCounter(EntityManager entityManager) {
        statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    public long count() {
        return statistics.getPrepareStatementCount();
    }
}
