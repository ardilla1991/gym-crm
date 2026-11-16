package com.epam.training.gym.api.rest.logging;

import com.epam.training.gym.api.rest.ApiPaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Logs two lines per REST call (paths under /api/v1): when it starts (endpoint) and when it ends
 * (endpoint, status, request and response bodies, duration). Lines of services and SQL are between them.
 * Passwords are masked: the request body of the password change is not logged at all, query parameters named
 * like "password" are masked. The Authorization header is never logged.
 * 2xx and 3xx are logged as INFO, 4xx as WARN, 5xx as ERROR.
 */
public class RequestLoggingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /** Longer bodies are cut in the log. */
    static final int MAX_LOGGED_BODY = 1000;

    /** "password": "..." and "newPassword": "..." in a JSON body. */
    private static final Pattern PASSWORD_FIELD =
            Pattern.compile("(\"(?:password|newPassword)\"\\s*:\\s*)\"[^\"]*\"");

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Only REST calls are logged: Swagger UI pages and scripts would only add noise
        if (!request.getRequestURI().startsWith(ApiPaths.API_V1)) {
            chain.doFilter(request, response);
            return;
        }
        // The wrappers remember the bodies while the controller reads and writes them
        ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper(request, MAX_LOGGED_BODY);
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        // The first line of the call: everything the call logs (services, SQL) comes after it.
        // The bodies are not known yet: they are read later by the controller, so they are logged at the end
        log.info("REST {} started", endpointForLog(request));
        long start = System.nanoTime();
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } finally {
            logCall(cachedRequest, cachedResponse, (System.nanoTime() - start) / 1_000_000);
            // The response body is still in the wrapper: send it to the client
            cachedResponse.copyBodyToResponse();
        }
    }

    private static void logCall(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                                long millis) {
        int status = response.getStatus();
        String endpoint = endpointForLog(request);
        Level level = status >= 500 ? Level.ERROR : status >= 400 ? Level.WARN : Level.INFO;

        log.atLevel(level).log("REST {} completed: {} in {} ms, request: {}, response: {}",
                endpoint, status, millis,
                // The new password can be written in JSON in many ways (escapes, spaces): this body is never logged
                isPasswordChange(request) ? "[omitted]" : forLog(request.getContentAsByteArray()),
                forLog(response.getContentAsByteArray()));
    }

    /**
     * Compares the route that Spring actually chose, not the raw URI: a raw URI may have extras
     * like "/password;probe=1" that Spring ignores when it finds the controller.
     */
    static boolean isPasswordChange(HttpServletRequest request) {
        return (ApiPaths.LOGIN + "/password").equals(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE));
    }

    /**
     * Method, path and query parameters. The parameters are taken decoded (so "pass%77ord" is "password");
     * a parameter named like "password" is masked: a client may put a password into the query too.
     */
    static String endpointForLog(HttpServletRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();
        String query = parameters.isEmpty() ? "" : "?" + parameters.entrySet().stream()
                .map(parameter -> parameter.getKey() + "="
                        + (parameter.getKey().toLowerCase(Locale.ROOT).contains("password")
                                ? "***" : String.join(",", parameter.getValue())))
                .collect(Collectors.joining("&"));
        return request.getMethod() + " " + request.getRequestURI() + query;
    }

    /** A body as one safe log line: passwords masked, line breaks removed, cut if too long. */
    static String forLog(byte[] body) {
        if (body.length == 0) {
            return "-";
        }
        String text = new String(body, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", " ");
        String masked = PASSWORD_FIELD.matcher(text).replaceAll("$1\"***\"");
        return masked.length() > MAX_LOGGED_BODY ? masked.substring(0, MAX_LOGGED_BODY) + "..." : masked;
    }

}
