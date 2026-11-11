package com.epam.training.gym.api.rest;

import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
public class WebConfig implements WebMvcConfigurer {

    /** Controller methods get the caller's Credentials from the Authorization header. */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CredentialsArgumentResolver());
    }
}
