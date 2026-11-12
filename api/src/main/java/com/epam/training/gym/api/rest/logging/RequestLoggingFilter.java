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
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Logs one line per REST call (paths under /api/v1): endpoint, request body, status, response body and duration.
 * Passwords are masked; the Authorization header is never logged.
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
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(cachedRequest, cachedResponse);
        } finally {
            logCall(cachedRequest, cachedResponse, System.currentTimeMillis() - start);
            // The response body is still in the wrapper: send it to the client
            cachedResponse.copyBodyToResponse();
        }
    }

    private static void logCall(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                                long millis) {
        int status = response.getStatus();
        String query = request.getQueryString();
        String endpoint = request.getMethod() + " " + request.getRequestURI() + (query != null ? "?" + query : "");
        Level level = status >= 500 ? Level.ERROR : status >= 400 ? Level.WARN : Level.INFO;

        log.atLevel(level).log("REST {} -> {} in {} ms, request: {}, response: {}",
                endpoint, status, millis,
                forLog(request.getContentAsByteArray()), forLog(response.getContentAsByteArray()));
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
