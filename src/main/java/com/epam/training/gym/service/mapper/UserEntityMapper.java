package com.epam.training.gym.service.mapper;

import com.epam.training.gym.model.User;
import com.epam.training.gym.persistence.entity.UserEntity;

/** Converts a user between the model used by the services and the database entity. */
public final class UserEntityMapper {

    private UserEntityMapper() {
    }

    /**
     * All fields except the password: a model read from the database never carries it.
     * The services check passwords on the entities; registration returns the generated password itself.
     */
    public static User toModel(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .username(entity.getUsername())
                .isActive(entity.isActive())
                .build();
    }

    /** A new row for a new user: the id is left empty, the database generates it. */
    public static UserEntity toNewEntity(User model) {
        return UserEntity.builder()
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .username(model.getUsername())
                .password(model.getPassword())
                .isActive(model.isActive())
                .build();
    }
}
