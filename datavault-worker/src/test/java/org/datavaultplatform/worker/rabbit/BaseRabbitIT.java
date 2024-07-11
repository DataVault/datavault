package org.datavaultplatform.worker.rabbit;

import jakarta.annotation.PostConstruct;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.util.UsesTestContainers;
import org.datavaultplatform.worker.config.RabbitConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

@UsesTestContainers
public abstract class BaseRabbitIT {

    public static final int HI_PRIORITY = 2;
    public static final int NORMAL_PRIORITY = 1;

    @Autowired
    AmqpAdmin amqpAdmin;
    
    @Autowired
    ApplicationContext ctx;
    
    @Autowired
    @Qualifier("monitorLogger")
    Logger log;

    @Container
    private static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImage.RABBIT_IMAGE_NAME)
            .withExposedPorts(5672, 15672)
            //.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(BaseRabbitIT.class)))
            .waitingFor(Wait.forLogMessage(".*started TCP listener on.*\\n", 1));

    
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @BeforeEach
    void setup(TestInfo info) {
        RabbitMessageSelectorScheduler scheduler = ctx.getBean(RabbitMessageSelectorScheduler.class);
        boolean realScheduler = scheduler != null && !mockingDetails(scheduler).isMock();
        if(realScheduler && log.getName().equals(RabbitConfig.class.getName())){
            Assertions.fail("log bean (monitorLogger) has not been overridden for tests");
        }
        log.info("info [{}]", info);
    }
    
    @PostConstruct
    void init() {
        assertThat(RABBIT.isCreated()).isTrue();
        log.info("RABBIT HTTP URL [ {} ]", RABBIT.getHttpUrl());
        log.info("rabbit username [{}]", RABBIT.getAdminUsername());
        log.info("rabbit password [{}]", RABBIT.getAdminPassword());
        log.info("rabbit host [{}]", RABBIT.getHost());
        log.info("rabbit AMQP port [{}]", RABBIT.getAmqpPort());
    }
}
