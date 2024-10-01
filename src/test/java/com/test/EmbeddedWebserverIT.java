package com.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.config.Configuration;

import jakarta.enterprise.inject.spi.CDI;

@ExtendWith(ArquillianExtension.class)
class EmbeddedWebserverIT {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedWebserverIT.class);

    private Configuration configuration;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(MavenImporter.class)
                .loadPomFromFile("pom.xml")
                .importBuildOutput()
                .as(WebArchive.class);

        log.debug("{}", webArchive.toString(true));

        return webArchive;
    }

    @BeforeEach
    public void setUp() {
        this.configuration = CDI.current().select(Configuration.class).get();
    }

    @Test
    void test() {
        assertThat(this.configuration.getProperty()).isNotBlank();
    }
}
