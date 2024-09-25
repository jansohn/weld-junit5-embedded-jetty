package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.file.PathUtils;
import org.jboss.weld.Container;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.config.Configuration;

import jakarta.enterprise.inject.spi.CDI;

@EnableAutoWeld
@AddBeanClasses(Configuration.class)
class EmbeddedJettyIT {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyIT.class);

    private Configuration configuration;

    private static final String CONTEXT_NAME = "/webapp";
    private static final Path RESOURCE_BASE = Paths.get("target", "webapp");

    private static Tomcat tomcat;
    private static Path docBase;

    @BeforeAll
    static void setUpBaseClass() throws Exception {
        // Check that necessary configuration files and resources exist
        if (Files.notExists(RESOURCE_BASE)) {
            throw new IllegalStateException("Resource base '" + RESOURCE_BASE.toAbsolutePath() + "' does not exist. Aborting!");
        }

        // create docBase directory for Tomcat and clean WEB-INF/lib directory to avoid ClassCastExceptions
        docBase = Files.createTempDirectory("tomcat_docbase_");
        PathUtils.copyDirectory(RESOURCE_BASE, docBase);
        PathUtils.cleanDirectory(docBase.resolve("WEB-INF/lib"));

        // Start the embedded webserver
        log.info("Starting embedded server...");
        startServer();
        log.info("Embedded server started: {}", getContextUrl());
    }

    @AfterAll
    static void tearDownBaseClass() throws Exception {
        PathUtils.deleteDirectory(docBase);

        // Stop the embedded Jetty webserver
        log.info("Stopping embedded Jetty server...");
        stopJettyServer();
        log.info("Embedded Jetty stopped!");
    }

    @BeforeEach
    void setUp() throws Exception {
        this.configuration = CDI.current().select(Configuration.class).get();
    }

    @AfterEach
    void tearDown() throws Exception {
        // nothing to do
    }

    private static void startServer() throws Exception {
        tomcat = new Tomcat();
        tomcat.setBaseDir(Paths.get("target", "catalina.base").toAbsolutePath().toString());
        tomcat.setPort(0);
        tomcat.enableNaming();

        Context ctx = tomcat.addWebapp("/webapp", docBase.toString());

        StandardContext standardCtx = (StandardContext) ctx;
        standardCtx.setClearReferencesRmiTargets(false);
        standardCtx.setClearReferencesThreadLocals(false);
        standardCtx.setSkipMemoryLeakChecksOnJvmShutdown(true);

        ctx.addApplicationListener(Listener.class.getName());
        // initialize Weld container with specific ID so that weld-junit does not re-use it
        ctx.addParameter(Container.CONTEXT_ID_KEY, UUID.randomUUID().toString());

        tomcat.getConnector();
        tomcat.start();
    }

    private static void stopJettyServer() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.getServer().await();
            tomcat.destroy();
        }
    }

    /**
     * Returns the URL of the deployed web application, e.g. <code>http://localhost:52185/webapp</code>
     * 
     * @return the URL of the deployed web application
     */
    public static String getContextUrl() {
        return tomcat != null ? String.format("http://localhost:%d%s", tomcat.getConnector().getLocalPort(), CONTEXT_NAME) : "";
    }

    @Test
    void test() {
        assertThat(this.configuration.getProperty()).isNotBlank();

    }
}
