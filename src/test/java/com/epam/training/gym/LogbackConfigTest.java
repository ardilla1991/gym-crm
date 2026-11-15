package com.epam.training.gym;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.OutputStreamAppender;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the real log format of the application from logback.xml. */
public class LogbackConfigTest {

    @Test
    void lineBreakInMessage_doesNotStartAFakeLogLine() throws Exception {
        // The application's logback.xml is loaded explicitly: tests themselves use the quieter logback-test.xml
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(getClass().getResource("/logback.xml"));
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        OutputStreamAppender<ILoggingEvent> console = (OutputStreamAppender<ILoggingEvent>) root.getAppender("CONSOLE");
        // The review's request: a decoded query parameter with CR LF
        LoggingEvent event = new LoggingEvent(Logger.class.getName(), root, Level.INFO,
                "REST GET /api/v1/health?q=first\r\nREVIEW-SYNTHETIC-LINE started", null, null);

        String written = new String(console.getEncoder().encode(event), StandardCharsets.UTF_8);

        // One record is one line: the only line break is the one at the end
        assertEquals(1, written.lines().count(), written);
        assertTrue(written.contains("first REVIEW-SYNTHETIC-LINE"), written);
        context.stop();
    }
}
