package com.epam.training.gym.api;

import com.epam.training.gym.domain.*;

/** Starts the Gym CRM REST server. Port: -Dserver.port=8080 (default). */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("server.port", 8080);
        GymApplication application = GymApplication.start(port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopQuietly(application)));
        application.await();
    }

    private static void stopQuietly(GymApplication application) {
        try {
            application.stop();
        } catch (Exception e) {
            // the JVM is shutting down anyway
        }
    }

}
