package com.test;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.config.Configuration;

@WebListener
public class ApplicationContextListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(ApplicationContextListener.class);

    @Inject
    private Configuration configuration;

    @Override
    public synchronized void contextInitialized(ServletContextEvent event) {
        log.info("web listener injected property: {}", configuration.getProperty());
    }

    @Override
    public synchronized void contextDestroyed(ServletContextEvent event) {
        log.info("Cleaning up webapp....");
    }
}
