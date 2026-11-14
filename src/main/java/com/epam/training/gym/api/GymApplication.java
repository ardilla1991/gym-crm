package com.epam.training.gym.api;

import com.epam.training.gym.api.rest.WebConfig;
import com.epam.training.gym.api.rest.logging.RequestLoggingFilter;
import com.epam.training.gym.api.rest.logging.TransactionIdFilter;
import com.epam.training.gym.config.AppConfig;
import jakarta.servlet.ServletRegistration;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Starts embedded Tomcat with two Spring contexts:
 * root (AppConfig: services, DAOs, database) and web (WebConfig: controllers).
 */
public final class GymApplication {

    private static final Logger log = LoggerFactory.getLogger(GymApplication.class);

    private final Tomcat tomcat;

    private GymApplication(Tomcat tomcat) {
        this.tomcat = tomcat;
    }

    /** @param port HTTP port;
     *  0 means any free port (used by tests) */
    public static GymApplication start(int port) throws LifecycleException, IOException {
        Path baseDir = Files.createTempDirectory("gym-tomcat");

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();   // creates the HTTP connector

        Context context = tomcat.addContext("", baseDir.toString());
        context.setParentClassLoader(GymApplication.class.getClassLoader());

        // Called by Tomcat when the web application starts:
        // registers Spring through the standard Servlet API
        context.addServletContainerInitializer((classes, servletContext) -> {
            // Root context: services, DAOs, database. Created on startup, closed on shutdown
            AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
            rootContext.register(AppConfig.class);
            servletContext.addListener(new ContextLoaderListener(rootContext));

            // Filters run before DispatcherServlet, in the order they are added:
            // first the transactionId, so that the request log line already has it
            servletContext.addFilter("transactionId", new TransactionIdFilter())
                    .addMappingForUrlPatterns(null, true, "/*");
            servletContext.addFilter("requestLogging", new RequestLoggingFilter())
                    .addMappingForUrlPatterns(null, true, "/*");

            // Web context: controllers. A child of the root context, owned by DispatcherServlet
            AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
            webContext.register(WebConfig.class);
            ServletRegistration.Dynamic dispatcher =
                    servletContext.addServlet("dispatcher", new DispatcherServlet(webContext));
            dispatcher.setLoadOnStartup(1);
            dispatcher.addMapping("/");
        }, null);

        tomcat.start();
        GymApplication application = new GymApplication(tomcat);
        log.info("Gym CRM started on http://localhost:{}", application.port());

        return application;
    }

    public int port() {
        return tomcat.getConnector().getLocalPort();
    }

    /** Blocks the calling thread while the server is running. */
    public void await() {
        tomcat.getServer().await();
    }

    public void stop() throws LifecycleException {
        tomcat.stop();
        tomcat.destroy();
        log.info("Gym CRM stopped");
    }
}
