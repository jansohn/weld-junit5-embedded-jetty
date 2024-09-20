package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.ee10.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee10.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.config.Configuration;

class EmbeddedJettyIT {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyIT.class);

    private Configuration configuration = new Configuration();

    private static final Path RESOURCE_BASE = Paths.get("target", "webapp");

    private static Server jettyServer;

    @BeforeAll
    static void setUpBaseClass() throws Exception {
        // Check that necessary configuration files and resources exist
        if (Files.notExists(RESOURCE_BASE)) {
            throw new IllegalStateException("Resource base '" + RESOURCE_BASE.toAbsolutePath() + "' does not exist. Aborting!");
        }

        // Start the embedded Jetty webserver
        log.info("Starting embedded Jetty server...");
        startJettyServer();
        log.info("Embedded Jetty server started: {}", getContextUrl());
    }

    @AfterAll
    static void tearDownBaseClass() throws Exception {
        // Stop the embedded Jetty webserver
        log.info("Stopping embedded Jetty server...");
        stopJettyServer();
        log.info("Embedded Jetty stopped!");
    }

    @BeforeEach
    void setUp() throws Exception {
        this.configuration.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        // nothing to do
    }

    private static void startJettyServer() throws Exception {
        jettyServer = new Server(0);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.setBaseResourceAsPath(RESOURCE_BASE);

        // Enable Weld + CDI
        context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        context.addServletContainerInitializer(new CdiServletContainerInitializer());
        context.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());

        jettyServer.setHandler(context);
        jettyServer.start();
        // jettyServer.dump(System.err);

        while (!jettyServer.isStarted()) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
        }

        log.debug("Jetty server started with context URL: {}", jettyServer.getURI());
    }

    private static void stopJettyServer() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();

            while (!jettyServer.isStopped()) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1L));
            }
        }
    }

    /**
     * Returns the URL of the deployed web application, e.g. <code>http://10.181.186.232:52185/webapp</code>
     * 
     * @return the URL of the deployed web application
     */
    public static String getContextUrl() {
        return jettyServer != null ? jettyServer.getURI().toString() : "";
    }

    @Test
    void test() {
        assertThat(this.configuration.getProperty()).isNotBlank();

    }
}
