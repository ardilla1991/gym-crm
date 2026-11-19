package com.epam.training.gym.api.rest.auth;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Checks the username and password of a REST call before Spring reads the request (body, query, @Valid)
 * and before the controller checks the owner: wrong credentials always give 401, before 400 or 403.
 * The services still check the password themselves, because they are also called without HTTP.
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final GymFacade facade;

    public AuthenticationInterceptor(GymFacade facade) {
        this.facade = facade;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Credentials credentials = CredentialsArgumentResolver.parse(request.getHeader(HttpHeaders.AUTHORIZATION));
        // A user is either a trainee or a trainer, so one successful check is enough
        if (!facade.matchTraineeCredentials(credentials) && !facade.matchTrainerCredentials(credentials)) {
            throw new AuthenticationException();
        }
        return true;
    }
}
