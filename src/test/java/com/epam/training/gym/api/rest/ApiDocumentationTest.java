package com.epam.training.gym.api.rest;

import com.epam.training.gym.api.rest.controller.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Swagger annotations do nothing at runtime, so this test makes sure no endpoint is left undocumented.
 * The annotations are on the *Api interfaces: AnnotatedElementUtils finds them through the controller.
 */
public class ApiDocumentationTest {

    @ParameterizedTest
    @ValueSource(classes = {AuthController.class, HealthController.class, TraineeController.class,
            TrainerController.class, TrainingController.class, TrainingTypeController.class})
    void everyEndpoint_isDocumented(Class<?> controller) {
        assertTrue(AnnotatedElementUtils.hasAnnotation(controller, Tag.class),
                controller.getSimpleName() + " has no @Tag");

        for (Method method : controller.getDeclaredMethods()) {
            // @GetMapping, @PostMapping... are themselves annotated with @RequestMapping
            if (AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
                String name = controller.getSimpleName() + "." + method.getName();
                assertTrue(AnnotatedElementUtils.hasAnnotation(method, Operation.class), name + " has no @Operation");
                assertFalse(AnnotatedElementUtils.findMergedRepeatableAnnotations(method, ApiResponse.class).isEmpty(),
                        name + " has no @ApiResponse");
            }
        }
    }

}
