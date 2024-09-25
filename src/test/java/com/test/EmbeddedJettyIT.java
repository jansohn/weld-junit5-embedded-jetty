package com.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.config.Configuration;

// @EnableWeld
// @TestInstance(Lifecycle.PER_CLASS)
class EmbeddedJettyIT {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyIT.class);

    private Configuration configuration = new Configuration();

    private static final String CONTEXT_NAME = "/webapp";
    private static final Path RESOURCE_BASE = Paths.get("target", "classes").toAbsolutePath();

    private static Tomcat tomcat;

    @BeforeAll
    static void setUpBaseClass() throws Exception {
        // Check that necessary configuration files and resources exist
        if (Files.notExists(RESOURCE_BASE)) {
            throw new IllegalStateException("Resource base '" + RESOURCE_BASE.toAbsolutePath() + "' does not exist. Aborting!");
        }

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
        String id = UUID.randomUUID().toString();

        // First boostrap Weld SE
        try (WeldContainer container = new Weld(id).initialize()) {
            TestBean testBean = container.select(TestBean.class).get();
            assertNotNull(testBean);

            // @Initialized(ApplicationScoped.class) ContainerInitialized
            List<Object> initEvents = testBean.getInitEvents();
            assertEquals(1, initEvents.size());
            Object event = initEvents.get(0);
            assertTrue(event instanceof ContainerInitialized);
            assertEquals(id, ((ContainerInitialized) event).getContainerId());

            tomcat = new Tomcat();
            tomcat.setBaseDir(Paths.get("target", "catalina.base").toAbsolutePath().toString());
            tomcat.setPort(0);
            tomcat.enableNaming();

            Context ctx = tomcat.addWebapp("/webapp", RESOURCE_BASE.toString());

            StandardContext standardCtx = (StandardContext) ctx;
            standardCtx.setClearReferencesRmiTargets(false);
            standardCtx.setClearReferencesThreadLocals(false);
            standardCtx.setSkipMemoryLeakChecksOnJvmShutdown(true);

            ctx.addApplicationListener(Listener.class.getName());
            ctx.getServletContext().setAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, container.getBeanManager());

            tomcat.getConnector();
            tomcat.start();
        }
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
