package com.epam.training.gym.service.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/** Checks a model (or a part of it) against its validation annotations before create and update. */
@Component
public class EntityValidator {

    private static final Logger log = LoggerFactory.getLogger(EntityValidator.class);

    private Validator validator;

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * @throws ConstraintViolationException if the model breaks any rule
     */
    public void validate(Object model) {
        Set<ConstraintViolation<Object>> violations = validator.validate(model);
        if (!violations.isEmpty()) {
            // Only field names and rule messages are logged, never the invalid values
            String details = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining(", "));
            log.warn("Validation failed for {}: {}", model.getClass().getSimpleName(), details);
            throw new ConstraintViolationException(violations);
        }
    }
}
