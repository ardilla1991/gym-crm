package com.epam.training.gym.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.training.gym.model.User;
import com.epam.training.gym.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

class UserEntityMapperTest {

    @Test
    void toModel_copiesAllFieldsButPassword() {
        UserEntity entity = UserEntity.builder()
                .id(7L).firstName("John").lastName("Smith")
                .username("John.Smith").password("Hq5sW1dF6y").isActive(true)
                .build();

        User model = UserEntityMapper.toModel(entity);

        assertEquals(7L, model.getId());
        assertEquals("John", model.getFirstName());
        assertEquals("Smith", model.getLastName());
        assertEquals("John.Smith", model.getUsername());
        assertNull(model.getPassword());             // a model read from the database never carries the password
        assertTrue(model.isActive());
    }

    @Test
    void toNewEntity_copiesFieldsButNotId() {
        User model = User.builder()
                .id(99L).firstName("John").lastName("Smith")
                .username("John.Smith").password("Hq5sW1dF6y").isActive(true)
                .build();

        UserEntity entity = UserEntityMapper.toNewEntity(model);

        assertNull(entity.getId());                  // a new row: the database generates the id
        assertEquals("John", entity.getFirstName());
        assertEquals("Smith", entity.getLastName());
        assertEquals("John.Smith", entity.getUsername());
        assertEquals("Hq5sW1dF6y", entity.getPassword());
        assertTrue(entity.isActive());
    }
}
