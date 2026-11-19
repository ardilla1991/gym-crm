package com.epam.training.gym.api.rest.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.charset.StandardCharsets;

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

    @ParameterizedTest
    @CsvSource({"200, INFO", "404, WARN", "500, ERROR"})
    void doFilter_anyStatus_reachesClientAndIsLoggedAtItsLevel(int status, String level) throws Exception {
        // The log lines go to a list, not to the console: their levels are checked here
        Logger filterLog = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        ListAppender<ILoggingEvent> logged = new ListAppender<>();
        logged.start();
        filterLog.addAppender(logged);
        filterLog.setAdditive(false);
        // Tests log only warnings (logback-test.xml): INFO is switched on here to check the level of a 200 call
        Level previousLevel = filterLog.getLevel();
        filterLog.setLevel(Level.INFO);
        try {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/trainees/John.Smith/trainings");
            request.setQueryString("periodFrom=2026-01-01");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> ((HttpServletResponse) res).setStatus(status));

            // The client gets the status unchanged; the final log line has the level of the status
            assertEquals(status, response.getStatus());
            ILoggingEvent finalLine = logged.list.get(logged.list.size() - 1);
            assertEquals(Level.toLevel(level), finalLine.getLevel());
        } finally {
            filterLog.detachAppender(logged);
            filterLog.setAdditive(true);
            filterLog.setLevel(previousLevel);
        }
    }

    @Test
    void isPasswordChange_usesTheRouteChosenBySpring() {
        // The raw URI has a path parameter, but Spring chose the password endpoint
        MockHttpServletRequest withPathParameter = new MockHttpServletRequest("PUT", "/api/v1/login/password;probe=1");
        withPathParameter.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/login/password");
        MockHttpServletRequest registration = new MockHttpServletRequest("POST", "/api/v1/trainees");
        registration.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/trainees");

        assertTrue(RequestLoggingFilter.isPasswordChange(withPathParameter));
        assertFalse(RequestLoggingFilter.isPasswordChange(registration));
    }

    @Test
    void endpointForLog_keepsFiltersAndMasksPassword() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/trainees/Emily.Johnson/trainings");
        request.addParameter("periodFrom", "2026-01-01");
        request.addParameter("password", "Hq5sW1dF6y");

        assertEquals("GET /api/v1/trainees/Emily.Johnson/trainings?periodFrom=2026-01-01&password=***",
                RequestLoggingFilter.endpointForLog(request));
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
