package com.epam.training.gym.persistence.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.epam.training.gym.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.epam.training.gym.persistence.storage.StorageNames.*;

/**
 * Fills the storage maps with prepared data from JSON files during application start.
 *
 * Spring calls postProcessAfterInitialization(...) for every bean it creates.
 * When the bean is one of the storage maps (recognised by its name),
 * the matching file is read and every record is put into the map by its id.
 */
@Component
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    /** Converts JSON into Java objects.
     * JavaTimeModule lets it read dates like "1998-05-14". */
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private Resource trainingTypeFile;
    private Resource traineeFile;
    private Resource trainerFile;
    private Resource trainingFile;

    // Paths come from application.properties. Spring turns "classpath:data/..." into a Resource (a file).

    @Value("${storage.init.trainee}")
    public void setTraineeFile(Resource traineeFile) {
        this.traineeFile = traineeFile;
    }

    @Value("${storage.init.trainer}")
    public void setTrainerFile(Resource trainerFile) {
        this.trainerFile = trainerFile;
    }

    @Value("${storage.init.training}")
    public void setTrainingFile(Resource trainingFile) {
        this.trainingFile = trainingFile;
    }

    @Value("${storage.init.training-type}")
    public void setTrainingTypeFile(Resource trainingTypeFile) {
        this.trainingTypeFile = trainingTypeFile;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        switch (beanName) {
            case TRAINEE_STORAGE -> loadData((Map<Long, Trainee>) bean, traineeFile,
                    new TypeReference<List<Trainee>>() { }, Trainee::getId);
            case TRAINER_STORAGE -> loadData((Map<Long, Trainer>) bean, trainerFile,
                    new TypeReference<List<Trainer>>() { }, Trainer::getId);
            case TRAINING_STORAGE -> loadData((Map<Long, Training>) bean, trainingFile,
                    new TypeReference<List<Training>>() { }, Training::getId);
            case TRAINING_TYPE_STORAGE -> loadData((Map<Long, TrainingType>) bean, trainingTypeFile,
                    new TypeReference<List<TrainingType>>() { }, TrainingType::getId);
            default -> {
                // not a storage bean: leave it untouched
            }
        }

        // Spring keeps whatever this method returns, so the bean must always be returned.
        return bean;
    }

    /**
     * Reads a JSON list from the file and puts every record into the storage by its id.
     *
     * @param type        what to read the JSON into, e.g. List<Trainee>
     * @param idExtractor how to get the id of a record, e.g. Trainee::getId
     */
    private <T> void loadData(Map<Long, T> storage, Resource resource,
                              TypeReference<List<T>> type, Function<T, Long> idExtractor) {

        try (InputStream in = resource.getInputStream()) {
            List<T> records = objectMapper.readValue(in, type);
            for (T entity  : records) {
                Long id = idExtractor.apply(entity );
                storage.put(id, entity );
            }
            log.info("Loaded {} records from {}", records.size(), resource.getFilename());
        } catch (IOException e) {
            throw new BeanInitializationException("Failed to read initial data from " + resource, e);
        }
    }

}
