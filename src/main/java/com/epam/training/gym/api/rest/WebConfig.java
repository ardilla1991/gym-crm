package com.epam.training.gym.api.rest;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.auth.AuthenticationInterceptor;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.logging.RequestLoggingFilter;
import com.epam.training.gym.api.rest.logging.TransactionIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** Web settings on top of Spring Boot's Spring MVC: logging filters, authentication, Credentials arguments. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final GymFacade facade;

    public WebConfig(GymFacade facade) {
        this.facade = facade;
    }

    /** Before requestLoggingFilter: the transactionId must be set before the request is logged. */
    @Bean
    public FilterRegistrationBean<TransactionIdFilter> transactionIdFilter() {
        FilterRegistrationBean<TransactionIdFilter> registration = new FilterRegistrationBean<>(new TransactionIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>(new RequestLoggingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }

    /**
     * Every REST call is authenticated before the controller, except registration
     * (only POST is mapped to these exact paths) and the health check.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor(facade))
                .addPathPatterns(ApiPaths.API_V1 + "/**")
                .excludePathPatterns(ApiPaths.TRAINEES, ApiPaths.TRAINERS, ApiPaths.HEALTH);
    }

    /** Controller methods get the caller's Credentials from the Authorization header. */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CredentialsArgumentResolver());
    }
}
