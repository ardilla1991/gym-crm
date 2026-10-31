package com.epam.training.gym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import com.epam.training.gym.domain.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.training.gym.persistence.storage.StorageNames.*;

/**
 * In-memory storage: one namespace = one java.util.Map = one Spring bean.
 * Key is the entity id, so listing one entity type is simply storage.values().
 * The maps are filled from files at startup by StorageInitializer (persistence module).
 */
@Configuration
public class StorageConfig {
    @Bean(TRAINEE_STORAGE)
    public Map<Long, Trainee> traineeStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(TRAINER_STORAGE)
    public Map<Long, Trainer> trainerStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(TRAINING_STORAGE)
    public Map<Long, Training> trainingStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean(TRAINING_TYPE_STORAGE)
    public Map<Long, TrainingType> trainingTypeStorage() {
        return new ConcurrentHashMap<>();
    }
}
