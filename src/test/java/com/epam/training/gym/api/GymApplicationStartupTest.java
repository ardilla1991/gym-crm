package com.epam.training.gym.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GymApplicationStartupTest {

    @Test
    void start_springFailsToStart_throwsInsteadOfRunningEmpty() {
        // The startup error is expected here: Spring and Tomcat logs are switched off for this test only
        Logger springLog = (Logger) LoggerFactory.getLogger("org.springframework");
        // Tomcat logs through java.util.logging and resets the levels of its own loggers on start,
        // so its console output is switched off instead
        java.util.logging.Handler[] tomcatOutput = java.util.logging.Logger.getLogger("").getHandlers();
        Level springLevel = springLog.getLevel();
        springLog.setLevel(Level.OFF);
        for (java.util.logging.Handler handler : tomcatOutput) {
            handler.setLevel(java.util.logging.Level.OFF);
        }
        // A database that does not exist: Spring can not create the connection pool
        System.setProperty("db.url", "jdbc:h2:tcp://localhost:1/nothing");
        try {
            assertThrows(IllegalStateException.class, () -> GymApplication.start(0));
        } finally {
            System.clearProperty("db.url");
            springLog.setLevel(springLevel);
            for (java.util.logging.Handler handler : tomcatOutput) {
                handler.setLevel(java.util.logging.Level.INFO);
            }
        }
    }
}
