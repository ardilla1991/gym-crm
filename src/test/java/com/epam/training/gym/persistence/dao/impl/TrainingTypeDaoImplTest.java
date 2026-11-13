package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
public class TrainingTypeDaoImplTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Test
    void findByName_returnsTypeWithItsId() {
        TrainingTypeEntity yoga = trainingTypeDao.findByName(TrainingTypeName.YOGA).orElseThrow();

        assertEquals(2L, yoga.getId());
    }

    /** Every enum value must exist in the training_type table. */
    @ParameterizedTest
    @EnumSource(TrainingTypeName.class)
    void findByName_everyEnumValue_isPresent(TrainingTypeName name) {
        assertTrue(trainingTypeDao.findByName(name).isPresent());
    }
}
