package com.epam.training.gym.api.rest.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void handleConstraintViolation_returnsBadRequest() {
        ResponseEntity<ApiError> response = handler.handleConstraintViolation(
                new ConstraintViolationException("invalid", Set.of()));

        assertEquals(400, response.getStatusCode().value());
    }

}
