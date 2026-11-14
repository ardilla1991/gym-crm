package com.epam.training.gym.api.rest.exception;

import com.epam.training.gym.api.rest.dto.ChangePasswordRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleOther_unexpectedException_returnsInternalErrorWithoutDetails() {
        ResponseEntity<ApiError> response = handler.handleOther(new IllegalStateException("db password is 123"));

        assertEquals(new ApiError(500, "Internal Server Error", "Internal server error"), response.getBody());
    }

    @Test
    void handleOther_springMvcException_keepsItsStatus() {
        ResponseEntity<ApiError> response = handler.handleOther(new HttpRequestMethodNotSupportedException("DELETE"));

        assertEquals(405, response.getStatusCode().value());
        assertEquals("Method Not Allowed", response.getBody().error());
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

}
