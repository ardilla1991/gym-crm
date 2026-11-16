package com.epam.training.gym.model.criteria;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainingCriteriaTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void blankTrainerName_meansNoFilter(String name) {
        assertNull(TraineeTrainingCriteria.builder().trainerName(name).build().trainerName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void blankTraineeName_meansNoFilter(String name) {
        assertNull(TrainerTrainingCriteria.builder().traineeName(name).build().traineeName());
    }

    @Test
    void names_areUsedWithoutSurroundingSpaces() {
        assertEquals("David", new TraineeTrainingCriteria(null, null, " David ", null).trainerName());
        assertEquals("Emily", new TrainerTrainingCriteria(null, null, " Emily ").traineeName());
    }
}
