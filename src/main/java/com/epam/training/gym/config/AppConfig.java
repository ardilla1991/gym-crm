package com.epam.training.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Root context: persistence, services and the facade.
 * The web layer has its own child context (WebConfig).
 */
@Configuration
// Configurations are imported, not scanned: test configurations (TestJpaConfig) share the packages
@ComponentScan(basePackages = {
        "com.epam.training.gym.persistence",
        "com.epam.training.gym.service",
        "com.epam.training.gym.api.facade"
}, excludeFilters = @ComponentScan.Filter(Configuration.class))
@Import(JpaConfig.class)
public class AppConfig {

    /** Resolves ${...} in @Value. Static: a BeanFactoryPostProcessor must exist before other beans. */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
