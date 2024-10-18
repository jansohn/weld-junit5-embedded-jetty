package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import com.test.config.Configuration;

import jakarta.inject.Inject;

@EnableAutoWeld
class ConfigurationIT {
    @Inject
    private Configuration configuration;

    @Test
    void test() {
        assertThat(this.configuration.getProperty()).isNotBlank();
    }
}
