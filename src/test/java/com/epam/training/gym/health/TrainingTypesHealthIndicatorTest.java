package com.epam.training.gym.health;

import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingTypesHealthIndicatorTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingTypesHealthIndicator indicator;

    @Test
    void health_allTypesStored_isUp() {
        when(trainingTypeDao.findAll()).thenReturn(Arrays.stream(TrainingTypeName.values())
                .map(name -> new TrainingType(null, name))
                .toList());

        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void health_typeMissing_isDownWithMissingTypes() {
        when(trainingTypeDao.findAll()).thenReturn(List.of(new TrainingType(1L, TrainingTypeName.YOGA)));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(Set.of(TrainingTypeName.FITNESS, TrainingTypeName.ZUMBA,
                TrainingTypeName.STRETCHING, TrainingTypeName.RESISTANCE), health.getDetails().get("missing"));
    }

    @Test
    void health_databaseFails_isDown() {
        when(trainingTypeDao.findAll()).thenThrow(new IllegalStateException("no connection"));

        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
