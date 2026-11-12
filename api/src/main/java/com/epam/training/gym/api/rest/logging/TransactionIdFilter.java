package com.epam.training.gym.api.rest.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Gives every REST call a transactionId: puts it into the MDC, so all log lines of the call
 * (controllers, services, DAOs, Hibernate SQL) contain it, and returns it in the response header.
 * A valid id sent by the caller in the same header is reused, so a call can be traced across systems.
 */
public class TransactionIdFilter extends HttpFilter {

    public static final String HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";

    /** Letters, digits and dashes only: an id from outside must not break or forge log lines. */
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String transactionId = transactionIdOf(request);
        MDC.put(MDC_KEY, transactionId);
        response.setHeader(HEADER, transactionId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private static String transactionIdOf(HttpServletRequest request) {
        String incoming = request.getHeader(HEADER);
        if (incoming != null && VALID_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }

}
