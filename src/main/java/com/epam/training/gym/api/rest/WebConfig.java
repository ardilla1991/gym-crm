package com.epam.training.gym.api.rest;

import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.configuration.SpringDocSpecPropertiesConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC configuration of the web (child) context: controllers and web settings.
 * Services, DAOs and the facade come from the root context built from AppConfig.
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.epam.training.gym.api.rest")
@Import({
        // springdoc without Spring Boot: the configuration that Boot would load automatically (springdoc FAQ).
        // Builds /v3/api-docs from the controllers and serves Swagger UI at /swagger-ui/index.html
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SpringDocSpecPropertiesConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        MultipleOpenApiSupportConfiguration.class,
        SwaggerConfig.class,
        SwaggerUiConfigProperties.class,
        SwaggerUiOAuthProperties.class,
        // Spring Boot web and MVC settings (spring.web.*, spring.mvc.*), needed by Swagger UI;
        // Boot would create these beans itself
        WebProperties.class,
        WebMvcProperties.class
})
public class WebConfig implements WebMvcConfigurer {

    /** Controller methods get the caller's Credentials from the Authorization header. */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CredentialsArgumentResolver());
    }
}
