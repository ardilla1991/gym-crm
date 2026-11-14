package com.epam.training.gym.api.rest.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionIdFilterTest {

    private final TransactionIdFilter filter = new TransactionIdFilter();

    @Test
    void doFilter_noHeader_generatesIdForLogsAndResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> idInLogs = new AtomicReference<>();

        // The chain stands for the controller: it sees the MDC as the rest of the call does
        filter.doFilter(new MockHttpServletRequest(), response,
                (req, res) -> idInLogs.set(MDC.get(TransactionIdFilter.MDC_KEY)));

        assertNotNull(idInLogs.get());
        assertEquals(idInLogs.get(), response.getHeader(TransactionIdFilter.HEADER));
        assertNull(MDC.get(TransactionIdFilter.MDC_KEY));   // cleared for the next request on this thread
    }

    @Test
    void doFilter_validIncomingId_isReused() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TransactionIdFilter.HEADER, "order-42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> idInLogs = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> idInLogs.set(MDC.get(TransactionIdFilter.MDC_KEY)));

        assertEquals("order-42", idInLogs.get());
        assertEquals("order-42", response.getHeader(TransactionIdFilter.HEADER));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "has spaces", "line\nbreak"})
    void doFilter_invalidIncomingId_isReplaced(String incoming) throws Exception {
        assertIncomingIdReplaced(incoming);
    }

    @Test
    void doFilter_tooLongIncomingId_isReplaced() throws Exception {
        assertIncomingIdReplaced("a".repeat(65));
    }

    private void assertIncomingIdReplaced(String incoming) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TransactionIdFilter.HEADER, incoming);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        String id = response.getHeader(TransactionIdFilter.HEADER);
        assertNotNull(id);
        assertNotEquals(incoming, id);
    }

}
