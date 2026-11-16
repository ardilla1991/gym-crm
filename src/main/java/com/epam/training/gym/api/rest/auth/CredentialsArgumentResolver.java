package com.epam.training.gym.api.rest.auth;

import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Fills a Credentials parameter of a controller method from the HTTP Basic Authorization header.
 * Only parses the header; the password is checked by AuthenticationInterceptor and the services.
 */
public class CredentialsArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BASIC_PREFIX = "Basic ";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Credentials.class;
    }

    @Override
    public Credentials resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return parse(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /** @throws AuthenticationException if the header is missing, not Basic, not Base64 or has no colon */
    static Credentials parse(String header) {
        // "Basic" is case-insensitive by the HTTP standard
        if (header == null || !header.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            throw new AuthenticationException("Authentication required");
        }
        String decoded = decode(header.substring(BASIC_PREFIX.length()).trim());
        int colon = decoded.indexOf(':');
        if (colon < 0) {
            throw new AuthenticationException("Invalid Authorization header");
        }
        // Only the first colon separates them: a password may contain colons
        return new Credentials(decoded.substring(0, colon), decoded.substring(colon + 1));
    }

    /** Invalid Base64 becomes a 401 instead of a 500. */
    private static String decode(String base64) {
        try {
            return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid Authorization header");
        }
    }
}
