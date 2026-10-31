package com.epam.training.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Root of the Spring application context.
 * Scans all modules for beans (@Component, @Repository, @Service, @Configuration)
 * and registers the external property file.
 */
@Configuration
@ComponentScan("com.epam.training.gym")
@PropertySource("classpath:application.properties")
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
}
