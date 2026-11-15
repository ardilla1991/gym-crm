package com.epam.training.gym.service.validation;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.User;
import com.epam.training.gym.model.validation.FieldLimits;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
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

    @ParameterizedTest
    @ValueSource(strings = {"Review:Colon", "Review/Slash", "Back\\slash", "Semi;colon", "Percent%", "John1",
            "John\tSmith", "John\nSmith", "\u007F",
            "Jos\u00e9",                  // "Jose" with an accent: not plain ASCII
            "\u0410\u043d\u043d\u0430"})  // Cyrillic "Anna"
    void validate_nameWithCharacterOutsideNames_fails(String firstName) {
        Trainee trainee = Trainee.builder().user(user(firstName, "Smith")).build();

        ConstraintViolationException exception =
                assertThrows(ConstraintViolationException.class, () -> entityValidator.validate(trainee));

        assertEquals(Set.of("user.firstName"), fields(exception));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Anna-Maria", "O'Neil", "Mary Ann"})
    void validate_usualNames_pass(String firstName) {
        Trainee trainee = Trainee.builder().user(user(firstName, "Smith")).build();

        assertDoesNotThrow(() -> entityValidator.validate(trainee));
    }

    @Test
    void validate_namesAtMaxLength_pass() {
        String name = "a".repeat(FieldLimits.NAME_MAX);
        Trainee trainee = Trainee.builder().user(user(name, name)).address("a".repeat(FieldLimits.ADDRESS_MAX)).build();

        assertDoesNotThrow(() -> entityValidator.validate(trainee));
    }

    @Test
    void validate_tooLongNamesAndAddress_reportAllFields() {
        String name = "a".repeat(FieldLimits.NAME_MAX + 1);
        Trainee trainee = Trainee.builder().user(user(name, name)).address("a".repeat(FieldLimits.ADDRESS_MAX + 1)).build();

        ConstraintViolationException exception =
                assertThrows(ConstraintViolationException.class, () -> entityValidator.validate(trainee));

        assertEquals(Set.of("user.firstName", "user.lastName", "address"), fields(exception));
    }

    @Test
    void validate_futureDateOfBirth_fails() {
        Trainee trainee = Trainee.builder().user(user("John", "Smith")).dateOfBirth(LocalDate.now().plusDays(1)).build();

        ConstraintViolationException exception =
                assertThrows(ConstraintViolationException.class, () -> entityValidator.validate(trainee));

        assertEquals(Set.of("dateOfBirth"), fields(exception));
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
