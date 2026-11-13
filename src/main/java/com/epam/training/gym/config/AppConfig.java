package com.epam.training.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Root of the Spring application context.
 * Scans all layers for beans (@Component, @Repository, @Service); configurations are imported explicitly.
 */
@Configuration
// Configurations are not scanned: in tests the classpath also has test configurations
// (TestJpaConfig) in the same packages, and they must not get into the application context
@ComponentScan(basePackages = "com.epam.training.gym",
        excludeFilters = @ComponentScan.Filter(Configuration.class))
@Import(JpaConfig.class)
public class AppConfig {

    /**
     * Resolves ${...} placeholders in @Value from application.properties.
     * Must be static: it is a BeanFactoryPostProcessor and has to exist
     * before any other bean of this class is created.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /** Bean Validation: checks @NotBlank, @NotNull... on entities. */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
