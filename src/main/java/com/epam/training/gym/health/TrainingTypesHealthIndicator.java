package com.epam.training.gym.health;

import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/** DOWN when a training type is missing in the database: trainings of that type can not be added. */
@Component
public class TrainingTypesHealthIndicator extends AbstractHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypesHealthIndicator.class);

    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypesHealthIndicator(TrainingTypeDao trainingTypeDao) {
        super("Training types health check failed");
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Set<TrainingTypeName> missing = EnumSet.allOf(TrainingTypeName.class);
        trainingTypeDao.findAll().forEach(type -> missing.remove(type.getTrainingTypeName()));
        if (missing.isEmpty()) {
            builder.up();
        } else {
            log.warn("Training types missing in the database: {}", missing);
            builder.down().withDetail("missing", missing);
        }
    }
}
