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
 * Logs two lines per call under /api/v1: "started" and "completed" with status, duration and bodies.
 * Passwords are masked; 4xx are logged as WARN, 5xx as ERROR.
 */
public class RequestLoggingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    static final int MAX_LOGGED_BODY = 1000;

    /** "password": "..." and "newPassword": "..." in a JSON body. */
    private static final Pattern PASSWORD_FIELD =
            Pattern.compile("(\"(?:password|newPassword)\"\\s*:\\s*)\"[^\"]*\"");

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!request.getRequestURI().startsWith(ApiPaths.API_V1)) {
            chain.doFilter(request, response);
            return;
        }
        // Keep copies of the bodies for the log
        ContentCachingRequestWrapper cachedRequest = new ContentCachingRequestWrapper(request, MAX_LOGGED_BODY);
        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        // Bodies are not read yet, so they are logged at the end
        log.info("REST {} started", endpointForLog(request));
        long start = System.nanoTime();
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } finally {
            logCall(cachedRequest, cachedResponse, (System.nanoTime() - start) / 1_000_000);
            // Without this the client gets an empty body
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
                // JSON can hide a password from the regex (escapes), so this body is never logged
                isPasswordChange(request) ? "[omitted]" : forLog(request.getContentAsByteArray()),
                forLog(response.getContentAsByteArray()));
    }

    /** Uses the route Spring matched, not the raw URI, which may have extras like ";probe=1". */
    static boolean isPasswordChange(HttpServletRequest request) {
        return (ApiPaths.LOGIN + "/password").equals(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE));
    }

    /** Method, path and decoded query parameters; parameters named like "password" are masked. */
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
