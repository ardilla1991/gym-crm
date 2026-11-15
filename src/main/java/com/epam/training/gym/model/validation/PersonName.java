package com.epam.training.gym.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * First or last name of a user: Latin letters, spaces, hyphens and apostrophes ("Anna-Maria", "O'Neil", "Mary Ann").
 * The names become the username (FirstName.LastName), which is used in HTTP Basic and in URL paths
 * (/trainees/{username}), so it must be plain ASCII (browsers encode Basic credentials only as Latin-1),
 * and any other character could break it: ':' splits Basic credentials (RFC 7617),
 * '/' and '\' split or break the path, ';' starts path parameters.
 * Allowing only name characters closes all such cases at once.
 * The rule is written once here and used by both the models and the REST requests.
 */
@NotBlank
@Size(max = FieldLimits.NAME_MAX)
@Pattern(regexp = FieldLimits.NAME_PATTERN, message = "must contain only Latin letters, spaces, hyphens and apostrophes")
@Constraint(validatedBy = {})
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
public @interface PersonName {

    String message() default "invalid name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
