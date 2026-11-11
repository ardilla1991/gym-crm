package com.epam.training.gym.api.rest.auth;

import com.epam.training.gym.domain.auth.Credentials;
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
 * Fills a controller method parameter of type Credentials from the HTTP Basic Authorization header:
 * {@code Authorization: Basic base64(username:password)}.
 * Only checks that the header is present and well-formed; the password itself is checked by the services.
 */
public class CredentialsArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String BASIC_PREFIX = "Basic ";

    /** Supports only parameters of type Credentials; all other parameters are left to Spring. */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Credentials.class;
    }

    /** Reads the Authorization header of the current request and turns it into Credentials. */
    @Override
    public Credentials resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return parse(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    /**
     * Parses an HTTP Basic Authorization header value.
     * Package-private so that the unit test can call it directly.
     *
     * @param header the header value, for example {@code Basic RW1pbHk6c2VjcmV0}; may be null
     * @return username and password from the header
     * @throws AuthenticationException if the header is missing, is not Basic, is not valid Base64
     *                                 or has no colon between username and password
     */
    static Credentials parse(String header) {
        // "Basic" is case-insensitive by the HTTP standard: "basic ..." is valid too
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

    /** Decodes Base64 to text; invalid Base64 becomes an authentication error instead of a 500. */
    private static String decode(String base64) {
        try {
            return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid Authorization header");
        }
    }
}
