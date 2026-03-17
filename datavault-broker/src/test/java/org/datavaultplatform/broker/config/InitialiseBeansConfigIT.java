package org.datavaultplatform.broker.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.controllers.admin.AdminController;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.AdminDepositService;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.util.UsesTestContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.broker.services.BaseEmailServiceTest.PORT_HTTP;
import static org.datavaultplatform.broker.services.BaseEmailServiceTest.PORT_SMTP;
import static org.datavaultplatform.common.ldap.BaseLDAPServiceIT.LDAP_ADMIN_PASSWORD;
import static org.datavaultplatform.common.ldap.BaseLDAPServiceIT.LDAP_EXPOSED_PORT;

/*
Added this test to check on spring bean wiring. 
When we use testcontainers for Rabbit, MariaDB, LDAP and EMAIL - will all the beans wire up.
There was a problem that AdminDepositService was not being created when the Broker was run as an application (not a test)
Note: AdminDepositService has '@ConditionalOnBean' - this is evaluated when the bean is first loaded, therefore, the order we
load java config files in DataVaultBrokerApp does matter.
 */
@SpringBootTest(classes = DataVaultBrokerApp.class)
@Slf4j
@AddTestProperties
@TestPropertySource(properties = {
        "broker.controllers.enabled=true",
        "broker.services.enabled=true",
        "broker.scheduled.enabled=true",
        "broker.rabbit.enabled=true",
        "broker.database.enabled=true",
        "auditdeposit.schedule=-",
        "encryptioncheck.schedule=-",
        "review.schedule=-",
        "delete.schedule=-",
        "retentioncheck.schedule=-"})
@UsesTestContainers
class InitialiseBeansConfigIT {
    
    @Container
    @ServiceConnection
    // This container is once per class - not once per method. Methods can 'dirty' the database.
    static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(DockerImage.MARIADB_IMAGE);

    @Container
    @ServiceConnection
    private static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImage.RABBIT_IMAGE_NAME)
            .withExposedPorts(5672,15672);

    @Container
    private static final GenericContainer<?> LDAP_CONTAINER = new GenericContainer<>(DockerImage.LDAP_IMAGE)
            .withEnv("LDAP_ROOT", "o=myu.ed")
            .withEnv("LDAP_ADMIN_PASSWORD", LDAP_ADMIN_PASSWORD)
            //SCHEMA - allows 'eduniRefNo' attributes - via LDIF file
            .withClasspathResourceMapping("ldap/eduniPersonSchema.ldif", "/schema/custom.ldif",
                    BindMode.READ_ONLY)
            //USERS via LDIF file
            .withClasspathResourceMapping("ldap/testUsers.ldif", "/custom/testUsers.ldif",
                    BindMode.READ_ONLY)
            .withEnv("LDAP_CUSTOM_LDIF_DIR", "/custom")
            .withExposedPorts(LDAP_EXPOSED_PORT)
            .withStartupCheckStrategy(
                    //Gotta allow time for openldap to initialise
                    new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(5))
            );


    @Container
    private static final GenericContainer<?> MAILHOG_CONTAINER
            = new GenericContainer<>(DockerImage.MAIL_IMAGE).withExposedPorts(PORT_SMTP, PORT_HTTP);

    @DynamicPropertySource
    static void setupProperties(DynamicPropertyRegistry registry) {
        setupMailProperties(registry);
    }
    
    public static void setupMailProperties(DynamicPropertyRegistry registry) {
        registry.add("tc.mailhog.http", () -> MAILHOG_CONTAINER.getMappedPort(PORT_HTTP));
        registry.add("mail.host", MAILHOG_CONTAINER::getHost);
        registry.add("mail.port", () -> MAILHOG_CONTAINER.getMappedPort(PORT_SMTP));
        log.info("email http://localhost:{}", MAILHOG_CONTAINER.getMappedPort(PORT_HTTP));
    }
    
    @Autowired
    AdminDepositService adminDepositService;
    
    @Autowired
    AdminController adminController;
    
    @Autowired
    Sender sender;
    
    @Test
    void testBeans() {
        assertThat(adminDepositService).isNotNull();
        assertThat(adminController).isNotNull();
        assertThat(sender).isNotNull();
    }
    
}
