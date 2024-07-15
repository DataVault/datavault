package org.datavaultplatform.worker.rabbit;

import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.util.UsesTestContainers;
import org.datavaultplatform.worker.config.RabbitConfig;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

@UsesTestContainers
@Timeout(value = 5, unit = TimeUnit.MINUTES, threadMode  = Timeout.ThreadMode.SEPARATE_THREAD)
public abstract class BaseRabbitIT {
    
    private static final Logger BASE_LOG = LoggerFactory.getLogger(BaseRabbitIT.class);

    public static final int HI_PRIORITY = 2;
    public static final int NORMAL_PRIORITY = 1;
    @Container
    private static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImage.RABBIT_IMAGE_NAME)
            .withExposedPorts(5672, 15672)
            //.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(BaseRabbitIT.class)))
            .waitingFor(
                    Wait.forLogMessage(".*started TCP listener on.*\\n", 1)
                            .withStartupTimeout(Duration.ofMinutes(2)));
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    ApplicationContext ctx;
    @Autowired
    @Qualifier("monitorLogger")
    Logger log;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        BASE_LOG.info("spring.rabbitmq.host [{}]", RABBIT.getHost());
        BASE_LOG.info("spring.rabbitmq.port [{}]", RABBIT.getAmqpPort());
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
    
    private boolean isServerListening(String host, int port) {
        Socket s = null;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 30_000);
            log.info("Socket Connect to socket [{}/{}] SUCCESS", host, port);
            return true;
        } catch (Exception ex) {
            log.error("Socket Connect to socket [{}/{}] FAILED", host, port);
            return false;
        } finally {
            IOUtils.closeQuietly(s);
        }
    }

    @BeforeEach
    void checkRabbitConnection() {

        assertThat(RABBIT.isCreated()).isTrue().withFailMessage(() -> "rabbit is NOT created");
        assertThat(RABBIT.isRunning()).isTrue().withFailMessage(() -> "rabbit is NOT running");
        log.info("RABBIT HTTP URL [ {} ]", RABBIT.getHttpUrl());
        log.info("rabbit username [{}]", RABBIT.getAdminUsername());
        log.info("rabbit password [{}]", RABBIT.getAdminPassword());
        log.info("rabbit host [{}]", RABBIT.getHost());
        log.info("rabbit AMQP port [{}]", RABBIT.getAmqpPort());

        // double check that we can connect via socket to rabbit before proceeding with actual tests
        assertThat(isServerListening(RABBIT.getHost(), RABBIT.getAmqpPort())).isTrue();
    }
}
