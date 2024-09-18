package com.test.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Configuration {
    private String property;

    @PostConstruct
    public void init() {
        this.property = "initialized!";
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
