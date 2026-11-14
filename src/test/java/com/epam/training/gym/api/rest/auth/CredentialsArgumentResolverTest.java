package com.epam.training.gym.api.rest.auth;

import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class CredentialsArgumentResolverTest {

    private final CredentialsArgumentResolver resolver = new CredentialsArgumentResolver();

    @Test
    void parse_validHeader_returnsUsernameAndPassword() {
        Credentials credentials = CredentialsArgumentResolver.parse(basic("Emily.Johnson:Hq5sW1dF6y"));

        assertEquals(new Credentials("Emily.Johnson", "Hq5sW1dF6y"), credentials);
    }

    @Test
    void parse_lowerCasePrefix_isAccepted() {
        String header = "basic " + base64("Emily.Johnson:secret");

        assertEquals("Emily.Johnson", CredentialsArgumentResolver.parse(header).username());
    }

    @Test
    void parse_passwordWithColon_keepsWholePassword() {
        Credentials credentials = CredentialsArgumentResolver.parse(basic("Emily.Johnson:pass:word"));

        assertEquals("pass:word", credentials.password());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Bearer abc", "Basic not-base64!", "Basic RW1pbHk="})   // the last one: "Emily", no colon
    void parse_missingOrInvalidHeader_throws(String header) {
        assertThrows(AuthenticationException.class, () -> CredentialsArgumentResolver.parse(header));
    }

    @Test
    void supportsParameter_onlyCredentials() throws NoSuchMethodException {
        var method = Endpoint.class.getDeclaredMethod("handle", Credentials.class, String.class);

        assertTrue(resolver.supportsParameter(new MethodParameter(method, 0)));
        assertFalse(resolver.supportsParameter(new MethodParameter(method, 1)));
    }

    @Test
    void resolveArgument_readsAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, basic("Anna.Lee:secret"));

        Credentials credentials = resolver.resolveArgument(null, null, new ServletWebRequest(request), null);

        assertEquals(new Credentials("Anna.Lee", "secret"), credentials);
    }

    private static String basic(String usernameAndPassword) {
        return "Basic " + base64(usernameAndPassword);
    }

    private static String base64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /** A method with a Credentials parameter and another one, to test supportsParameter. */
    private static final class Endpoint {
        @SuppressWarnings("unused")
        void handle(Credentials credentials, String other) {
        }
    }
}
