package com.epam.training.gym.persistence.entity;

/** A trainee or a trainer row: a profile that has a user account. */
public interface ProfileEntity {

    UserEntity getUser();
}
