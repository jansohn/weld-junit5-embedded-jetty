package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.config.Configuration;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

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
