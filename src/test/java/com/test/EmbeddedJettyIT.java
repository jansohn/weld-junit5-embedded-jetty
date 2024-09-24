package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.config.Configuration;

import jakarta.enterprise.inject.spi.CDI;

@EnableAutoWeld
@TestInstance(Lifecycle.PER_CLASS)
class EmbeddedJettyIT {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyIT.class);

    private Configuration configuration = new Configuration();

    private static final String CONTEXT_NAME = "/webapp";
    private static final Path RESOURCE_BASE = Paths.get("target", "webapp");
    private static Path webXml;

    private static Tomcat tomcat;

    @BeforeAll
    static void setUpBaseClass() throws Exception {
        // Check that necessary configuration files and resources exist
        if (Files.notExists(RESOURCE_BASE)) {
            throw new IllegalStateException("Resource base '" + RESOURCE_BASE.toAbsolutePath() + "' does not exist. Aborting!");
        }

        URL url = EmbeddedJettyIT.class.getClassLoader().getResource("WEB-INF/web.xml");
        if (url == null) { throw new IllegalStateException("WEB-INF/web.xml descriptor not found in classpath. Aborting!"); }
        webXml = Paths.get(url.toURI());

        // Start the embedded webserver
        log.info("Starting embedded server...");
        startServer();
        log.info("Embedded server started: {}", getContextUrl());
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

    private static void startServer() throws Exception {
        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.enableNaming();

        Context ctx = tomcat.addWebapp("/webapp", RESOURCE_BASE.toAbsolutePath().toString());

        StandardContext standardCtx = (StandardContext) ctx;
        standardCtx.setClearReferencesRmiTargets(false);
        standardCtx.setClearReferencesThreadLocals(false);
        standardCtx.setSkipMemoryLeakChecksOnJvmShutdown(true);

        ctx.addApplicationListener(Listener.class.getName());
        ctx.getServletContext().setAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, CDI.current().getBeanManager());

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
