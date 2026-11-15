package com.epam.training.gym.api.rest.exception;

import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidInputException;
import com.epam.training.gym.service.exception.InvalidStateException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Turns exceptions from all controllers into HTTP statuses with an ApiError body.
 * Messages never contain stack traces or class names.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Wrong or missing username and password: 401 with a Basic challenge, as HTTP requires. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Gym CRM\"")
                .body(apiError(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    /** The signed-in user tries to change data of another user. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e) {
        return respond(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /** The request conflicts with the current state, for example activating an active profile. */
    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ApiError> handleInvalidState(InvalidStateException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    /**
     * The data conflicts with data saved at the same moment, for example two registrations of the same name
     * chose one username: the unique index rejected the second one. Repeating the request generates a new username.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataConflict(DataIntegrityViolationException e) {
        // The database message has table and column names: it is logged, not sent to the client
        log.warn("Data conflict: {}", e.getMostSpecificCause().getMessage());
        return respond(HttpStatus.CONFLICT, "The data was changed at the same time by another request, please repeat");
    }

    /** There is no trainee, trainer or training type with the given name. */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** @Valid failed: lists every invalid field, for example "firstName: must not be blank". */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    /** The body is not valid JSON or has a wrong value, for example an unknown training type. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException e) {
        // The exception message contains Java class names, so it is not shown to the client
        return respond(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON");
    }

    /** A path or query parameter has a wrong format, for example periodFrom=abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return respond(HttpStatus.BAD_REQUEST,
                "Invalid value '" + e.getValue() + "' of parameter '" + e.getName() + "'");
    }

    /** Service validation failed (EntityValidator): the same 400 as for an invalid request body. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    /** A service rule for the input failed, for example a repeated trainer in the list. */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiError> handleInvalidInput(InvalidInputException e) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * Everything else. Spring MVC exceptions (unknown path, wrong HTTP method...) keep their own status and headers;
     * unexpected exceptions become 500 without details, so no internals leak to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception e) {
        if (e instanceof ErrorResponse springError) {
            String detail = springError.getBody().getDetail();
            // Spring's own headers are kept: for example Allow for 405, which HTTP requires
            return ResponseEntity.status(springError.getStatusCode())
                    .headers(springError.getHeaders())
                    .body(apiError(springError.getStatusCode(), Objects.requireNonNullElse(detail, e.getMessage())));
        }
        // The client gets no details, so the log must have them all; the line also has the transactionId
        log.error("Unexpected error", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private static ResponseEntity<ApiError> respond(HttpStatusCode status, String message) {
        return ResponseEntity.status(status).body(apiError(status, message));
    }

    private static ApiError apiError(HttpStatusCode status, String message) {
        HttpStatus known = HttpStatus.resolve(status.value());
        String reason = known != null ? known.getReasonPhrase() : "Error";
        return new ApiError(status.value(), reason, message);
    }

}
