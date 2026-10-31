package com.epam.training.gym.service.exception;

/** Thrown when an entity with the given id does not exist in the storage. */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + " with id " + id + " not found");
    }
}
