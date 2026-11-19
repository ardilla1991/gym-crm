package com.epam.training.gym.api.rest.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.epam.training.gym.api.rest.dto.ChangePasswordRequest;
import com.epam.training.gym.service.exception.InvalidInputException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleOther_unexpectedException_returnsInternalErrorWithoutDetails() {
        // The expected error goes to a list, not to the console: it is checked here instead of being printed
        Logger handlerLog = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> logged = new ListAppender<>();
        logged.start();
        handlerLog.addAppender(logged);
        handlerLog.setAdditive(false);
        try {
            ResponseEntity<ApiError> response = handler.handleOther(new IllegalStateException("db password is 123"));

            // The client gets no details; the log gets all of them, with the stack trace
            assertEquals(new ApiError(500, "Internal Server Error", "Internal server error"), response.getBody());
            assertEquals(Level.ERROR, logged.list.get(0).getLevel());
            assertNotNull(logged.list.get(0).getThrowableProxy());
        } finally {
            handlerLog.detachAppender(logged);
            handlerLog.setAdditive(true);
        }
    }

    @Test
    void handleOther_springMvcException_keepsItsStatusAndHeaders() {
        ResponseEntity<ApiError> response = handler.handleOther(
                new HttpRequestMethodNotSupportedException("DELETE", List.of("GET")));

        assertEquals(405, response.getStatusCode().value());
        assertEquals("Method Not Allowed", response.getBody().error());
        // HTTP requires Allow in a 405 response: which methods the path does support
        assertEquals(Set.of(HttpMethod.GET), response.getHeaders().getAllow());
    }

    @Test
    void handleConstraintViolation_namesInvalidField() {
        // A real violation from a real validator: a blank new password
        Set<ConstraintViolation<ChangePasswordRequest>> violations;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            violations = factory.getValidator().validate(new ChangePasswordRequest(" "));
        }

        ResponseEntity<ApiError> response = handler.handleConstraintViolation(
                new ConstraintViolationException(violations));

        assertEquals(400, response.getStatusCode().value());
        // Only the field name is checked: the message language depends on the system locale
        String message = response.getBody().message();
        assertTrue(message.startsWith("newPassword: "), message);
    }

    @Test
    void handleInvalidInput_returnsBadRequestWithMessage() {
        ResponseEntity<ApiError> response = handler.handleInvalidInput(
                new InvalidInputException("Trainer usernames must not repeat: [David.Miller, David.Miller]"));

        assertEquals(new ApiError(400, "Bad Request", "Trainer usernames must not repeat: [David.Miller, David.Miller]"),
                response.getBody());
    }

    @Test
    void handleDataConflict_returnsConflictWithoutDatabaseDetails() {
        ResponseEntity<ApiError> response = handler.handleDataConflict(
                new DataIntegrityViolationException("Unique index violation: USERS(USERNAME)"));

        assertEquals(409, response.getStatusCode().value());
        assertFalse(response.getBody().message().contains("USERS"), response.getBody().message());
    }

}
