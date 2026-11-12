package com.epam.training.gym.api.rest.logging;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

public class RequestLoggingFilterTest {

    private static final String REGISTRATION_JSON = "{\"username\":\"John.Smith\",\"password\":\"secret\"}";

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void doFilter_keepsStatusAndResponseBodyForClient() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("POST", "/api/v1/trainees"), response, (req, res) -> {
            ((HttpServletResponse) res).setStatus(201);
            res.getOutputStream().write(REGISTRATION_JSON.getBytes(StandardCharsets.UTF_8));
        });

        // Masking is only for the log: the client gets the real password
        assertEquals(201, response.getStatus());
        assertEquals(REGISTRATION_JSON, response.getContentAsString());
    }

    @Test
    void doFilter_notRestPath_passesOriginalResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Swagger UI files are not wrapped and not logged
        filter.doFilter(new MockHttpServletRequest("GET", "/swagger-ui/index.html"), response,
                (req, res) -> assertSame(response, res));
    }

    @Test
    void forLog_masksPassword() {
        assertEquals("{\"username\":\"John.Smith\",\"password\":\"***\"}",
                RequestLoggingFilter.forLog(REGISTRATION_JSON.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void forLog_masksNewPasswordWithSpaces() {
        assertEquals("{\"newPassword\": \"***\"}",
                RequestLoggingFilter.forLog("{\"newPassword\": \"s3cret\"}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void forLog_lineBreaks_becomeSpaces() {
        assertEquals("{ \"a\": 1 }", RequestLoggingFilter.forLog("{\n\"a\": 1\r\n}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void forLog_emptyBody_isDash() {
        assertEquals("-", RequestLoggingFilter.forLog(new byte[0]));
    }

    @Test
    void forLog_longBody_isCut() {
        String logged = RequestLoggingFilter.forLog("a".repeat(1500).getBytes(StandardCharsets.UTF_8));

        assertEquals(RequestLoggingFilter.MAX_LOGGED_BODY + 3, logged.length());
        assertTrue(logged.endsWith("..."));
    }

}
