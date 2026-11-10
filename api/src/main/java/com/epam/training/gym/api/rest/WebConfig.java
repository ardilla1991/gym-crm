package com.epam.training.gym.api.rest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC configuration of the web (child) context: controllers and web settings.
 * Services, DAOs and the facade come from the root context built from AppConfig.
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.epam.training.gym.api.rest")
public class WebConfig implements WebMvcConfigurer {
}
