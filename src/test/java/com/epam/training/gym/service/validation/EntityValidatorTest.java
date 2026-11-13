package com.epam.training.gym.service.validation;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.User;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class EntityValidatorTest {

    private static ValidatorFactory factory;
    private static EntityValidator entityValidator;

    @BeforeAll
    static void createValidator() {
        // The real Hibernate Validator, without Spring
        factory = Validation.buildDefaultValidatorFactory();
        entityValidator = new EntityValidator();
        entityValidator.setValidator(factory.getValidator());
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void validate_traineeWithNames_passes() {
        Trainee trainee = Trainee.builder().user(user("John", "Smith")).build();

        assertDoesNotThrow(() -> entityValidator.validate(trainee));
    }

    @Test
    void validate_blankNames_reportsBothFields() {
        Trainee trainee = Trainee.builder().user(user(" ", null)).build();

        ConstraintViolationException exception =
                assertThrows(ConstraintViolationException.class, () -> entityValidator.validate(trainee));

        assertEquals(Set.of("user.firstName", "user.lastName"), fields(exception));
    }

    @Test
    void validate_trainerWithoutSpecialization_fails() {
        Trainer trainer = Trainer.builder().user(user("Anna", "Lee")).build();

        ConstraintViolationException exception =
                assertThrows(ConstraintViolationException.class, () -> entityValidator.validate(trainer));

        assertEquals(Set.of("specialization"), fields(exception));
    }

    private static User user(String firstName, String lastName) {
        return User.builder().firstName(firstName).lastName(lastName).build();
    }

    private static Set<String> fields(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
